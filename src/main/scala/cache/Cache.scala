package cache

/** Generic cache interface.
  *
  * @tparam T values of type `T` can be stored in cache
  */
trait Cache[T] {
  /** Accesses the cache and returns the value or `None` if no entry exists.
    *
    * @param key  cached key
    * @return  value if exists or `None` otherwise
    */
  def get(key: String): Option[T]

  /** Stores a new value under the provided key in the cache.
    * Overwrites the cached value if it already exists in the cache.
    *
    * @param key  cached key
    * @param value  value to be stored
    * @return   inserted value, or `None` if value could not be stored in cache
    */
  def put(key: String, value: T): Option[T]

  /** Remove entry with key from cache.
    *
    * @param key  key for entry to be removed
    */
  def remove(key: String): Unit
}
