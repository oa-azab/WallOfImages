package omar.example.com.resourceloaderlibrary


/**
 * Loading resource result class that represents both success and errors
 */
sealed class ResourceLoadResult<T>

/**
 * Passed to listener when loading resource was successful
 *
 * @param data the result
 */
class ResourceLoadSuccess<T>(val data: T) : ResourceLoadResult<T>()

/**
 * Passed to listener when the loading resource failed
 *
 * @param error the exception that caused this error
 */
class ResourceLoadError<T>(val error: Throwable) : ResourceLoadResult<T>()