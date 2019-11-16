package redis

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{BeforeAndAfterEach, FlatSpec}

class RedisConnectorSpec extends FlatSpec with BeforeAndAfterEach {
  val config: Config = ConfigFactory.load()
  val redis = new RedisConnector(config.getString("redis.host"), config.getInt("redis.port"))

  override def beforeEach(): Unit = {
    redis.clear()
  }

  "Redis Connector" should "connect to backed redis" in {
    assert(redis.isConnected)
  }

  it should "be empty when new instance is created" in {
    assert(redis.size() == 0)
  }

  it should "remove all keys" in {
    redis.clear()
    assert(redis.size() == 0)
  }

  it should "store key value pair" in {
    assert(redis.size() == 0)
    redis.put("key1", "test")
    assert(redis.size() == 1)
  }

  it should "retrieve stored value" in {
    redis.put("key1", "test")
    redis.put("key2", 123)
    redis.put("key3", true)

    assert(redis.get("key2").contains("123"))
    assert(redis.get("key1").contains("test"))
    assert(redis.get("key3").contains("true"))
  }

  it should "return None for non-existing key" in {
    assert(redis.get("key2").isEmpty)
  }
}
