import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import cache.{Cache, LruCache}
import com.typesafe.config.ConfigFactory
import org.joda.time.Duration
import redis.RedisConnector
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.ExecutionContextExecutor

object Main extends App with RedisProxyService {
  implicit val system: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val config = ConfigFactory.load()
  override val cache = new LruCache[Any](
    config.getInt("cache.maxSize"),
    new Duration(config.getInt("cache.maxLifeTime")))
  override val redis = new RedisConnector(config.getString("redis.host"), config.getInt("redis.port"))

  // start webservice
  Http().bindAndHandle(route, config.getString("http.host"), config.getInt("http.port"))
}


/** Handles requests and defines existing routes. */
trait RedisProxyService extends SprayJsonSupport with DefaultJsonProtocol {
  val cache: Cache[Any]
  val redis: RedisConnector

  /** Represents a key value pair. */
  case class KeyValuePair(key: String, value: String)

  /** Specifies JSON de/serialization of `KeyValuePair`. */
  implicit val KeyValueFormats: RootJsonFormat[KeyValuePair] = jsonFormat2(KeyValuePair)

  val route: Route =
    (get & path("GET" / Segment)) { key =>
      cache.get(key) match {
        case Some(v) => complete(v.toString)
        case None =>
          redis.get(key) match {
            case Some(v) => complete(v.toString)
            case None => complete(StatusCodes.NotFound, "Entry does not exist.")
          }
      }
    } ~
    (post & path("SET")) {
      entity(as[KeyValuePair]) { kv =>
        cache.put(kv.key, kv.value) match {
          case None => complete(StatusCodes.InternalServerError, "Could not store key value pair in cache.")
          case Some(_) =>
            if (redis.put(kv.key, kv.value)) {
              complete(StatusCodes.Created)
            } else {
              complete(StatusCodes.InternalServerError, "Could not store key value pair in Redis.")
            }
        }
      }
    } ~
    (delete & path("DEL" / Segment)) { key =>
      cache.remove(key)
      redis.remove(key)
      complete(StatusCodes.OK)
    }
}
