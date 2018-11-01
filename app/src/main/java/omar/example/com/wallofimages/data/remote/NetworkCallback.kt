package omar.example.com.wallofimages.data.remote


/**
 * Network result class that represents both success and errors
 */
sealed class NetworkResult<T>

/**
 * Passed to listener when the network request was successful
 *
 * @param data the result
 */
class NetworkSuccess<T>(val data: T) : NetworkResult<T>()

/**
 * Passed to listener when the network failed
 *
 * @param error the exception that caused this error
 */
class NetworkError<T>(val error: Throwable) : NetworkResult<T>()