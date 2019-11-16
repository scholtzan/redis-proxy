package cache

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap
import org.joda.time.{DateTime, Duration}

/** Least-recently used cache.
  *
  * @param maxSize  maximum cache size
  * @param maxLiveTime  maximum age of entries
  * @tparam T   values of type `T` can be stored in cache
  */
final class LruCache[T](maxSize: Int, maxLiveTime: Duration) extends Cache[T] {
  private case class Entry(
    value: T,
    var age: DateTime
  )

  // internal thread-safe storage for cache
  private val cache = new ConcurrentLinkedHashMap.Builder[String, Entry].maximumWeightedCapacity(maxSize).build()

  override def get(key: String): Option[T] = {
    cache.get(key) match {
      case null => None
      case entry: Entry =>
        remove(key)
        if (isExpired(entry)) {
          None
        } else {
          // update last accessed timestamp
          cache.put(key, entry.copy(age = DateTime.now()))
          Some(entry.value)
        }
    }
  }

  override def put(key: String, value: T): Option[T] = {
    val newEntry = Entry(value, DateTime.now())

    cache.put(key, newEntry) match {
      case null => None
      case entry => Some(entry.value)
    }
  }

  override def remove(key: String): Unit = {
    cache.remove(key)
  }

  /** Check if entry exceeded maximum lifetime.
    *
    * @param entry  entry to be checked
    * @return true, if the entry is expired
    */
  private def isExpired(entry: Entry): Boolean = {
    entry.age.plus(maxLiveTime).isBeforeNow
  }

  /** Clear cache. */
  def clear(): Unit = {
    cache.clear()
  }

  /** Returns the number of keys stored in the cache. */
  def size(): Int = {
    cache.size()
  }
}
