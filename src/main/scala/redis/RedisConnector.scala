package redis

import com.redis.RedisClient

/** Handles connection and operations on Redis.
  *
  * @param address  host address of backing Redis
  * @param port     port of backing redis
  */
class RedisConnector(address: String, port: Int) {
  private val client = new RedisClient(address, port)

  /** Write key value pair to Redis.
    *
    * @return true, if write was successful
    */
  def put(key: String, value: Any): Boolean = {
    client.set(key, value)
  }

  /** Retrieve value for key from Redis.
    *
    * @return value if exists, otherwise `None`
    */
  def get(key: String): Option[Any] = {
    client.get(key)
  }

  /** Remove all keys from Redis. */
  def clear(): Unit = {
    client.flushall
  }

  /** Remove specific key from Redis.
    *
    * @param key  key to be removed
    */
  def remove(key: String): Unit = {
    client.del(key)
  }

  /** Returns connections status of Redis connector. */
  def isConnected: Boolean = {
    client.connected
  }

  /** Returns the number of keys stored in Redis. */
  def size(): Int = {
    client.keys() match {
      case Some(keys) => keys.length
      case None => 0
    }
  }
}
