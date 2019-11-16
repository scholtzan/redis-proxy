import akka.http.scaladsl.model.{ContentType, HttpEntity, MediaTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cache.LruCache
import com.typesafe.config.{Config, ConfigFactory}
import org.joda.time.Duration
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import redis.RedisConnector

class RedisProxyServiceSpec extends WordSpec with Matchers with BeforeAndAfterEach with ScalatestRouteTest with RedisProxyService {
  val config: Config = ConfigFactory.load()
  override val cache = new LruCache[Any](config.getInt("cache.maxSize"), new Duration(config.getInt("cache.maxLifeTime")))
  override val redis = new RedisConnector(config.getString("redis.host"), config.getInt("redis.port"))

//  override def beforeEach(): Unit = {
//    cache.clear()
//    redis.clear()
//  }

  "Redis Proxy Service" should {
    "get 404 for accessing non-existing key" in {
      Get("/GET/key1") ~> route ~> check {
        status shouldEqual StatusCodes.NotFound
      }
    }

    "get value from redis for existing key if not in cache" in {
      redis.put("key1", "test")

      Get("/GET/key1") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldEqual "test"
      }
    }

    "get value from cache for existing key" in {
      cache.put("key1", "test")

      Get("/GET/key1") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldEqual "test"
      }
    }

    "create new key value pair" in {
      Post("/SET", HttpEntity(ContentType(MediaTypes.`application/json`), """{ "key": "key1", "value" : "42" }""")) ~>
        route ~> check {
        status shouldEqual StatusCodes.Created
        assert(redis.get("key1").contains("42"))
        assert(cache.get("key1").contains("42"))
      }
    }

    "overwrite existing key value pair" in {
      redis.put("key1", "test")
      cache.put("key1", "test")

      Post("/SET", HttpEntity(ContentType(MediaTypes.`application/json`), """{ "key": "key1", "value" : "42" }""")) ~>
        route ~> check {
        status shouldEqual StatusCodes.Created
        assert(redis.get("key1").contains("42"))
        assert(cache.get("key1").contains("42"))
      }
    }

    "remove existing key" in {
      redis.put("key1", "test")
      cache.put("key1", "test")

      Delete("/DEL/key1") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        assert(redis.get("key1").isEmpty)
        assert(cache.get("key1").isEmpty)
      }
    }

    "remove non-existing key" in {
      Delete("/DEL/key1") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        assert(redis.get("key1").isEmpty)
        assert(cache.get("key1").isEmpty)
      }
    }
  }
}
