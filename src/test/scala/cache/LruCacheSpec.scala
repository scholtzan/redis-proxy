package cache

import org.joda.time.Duration
import org.scalatest.{BeforeAndAfterEach, FlatSpec}

class LruCacheSpec extends FlatSpec with BeforeAndAfterEach {
  val cache = new LruCache[Any](3, new Duration(10000))

  override def beforeEach(): Unit = {
    cache.clear()
  }

  "LRU cache" should "be empty after creation" in {
    assert(cache.size() == 0)
  }

  it should "store values" in {
    assert(cache.size() == 0)
    cache.put("key1", "test")
    assert(cache.size() == 1)
  }

  it should "retrieve stored values" in {
    cache.put("key1", "test")
    cache.put("key2", 123)

    assert(cache.get("key1").contains("test"))
    assert(cache.get("key2").contains(123))
  }

  it should "return None for non-existing keys" in {
    assert(cache.get("key2").isEmpty)
  }

  it should "get cleared" in {
    assert(cache.size() == 0)
    cache.put("key1", "test")
    cache.put("key2", "test")
    assert(cache.size() == 2)
    cache.clear()
    assert(cache.size() == 0)
  }

  it should "overwrite existing values" in {
    cache.put("key1", "test")
    assert(cache.get("key1").contains("test"))
    cache.put("key1", 123)
    assert(cache.get("key1").contains(123))
  }

  it should "remove least-recently used values" in {
    cache.put("key1", "test")
    assert(cache.get("key1").contains("test"))

    cache.put("key2", 123)
    cache.put("key3", true)
    cache.put("key4", "foo")

    assert(cache.get("key1").isEmpty)
    assert(cache.get("key2").contains(123))
    assert(cache.get("key4").contains("foo"))

    val _ = cache.get("key2") // access to value makes it "recent" again
    assert(cache.get("key2").contains(123))

    cache.put("key5", 555)

    assert(cache.get("key3").isEmpty)
    assert(cache.get("key2").contains(123))
  }

  it should "remove entries that exceeded the maximum life time" in {
    cache.put("key1", "test")
    Thread.sleep(9000)
    cache.put("key2", 123)
    assert(cache.size() == 2)
    Thread.sleep(2000)
    assert(cache.get("key1").isEmpty)
    assert(cache.get("key2").contains(123))
  }
}
