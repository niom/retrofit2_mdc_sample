import okhttp3.Request
import okio.Timeout
import org.slf4j.MDC
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

class MdcCallAdapter<R : Any>(private val responseType: Type): CallAdapter<R, Call<R>> {
    private val mdcContextCopy: MutableMap<String, String> = mutableMapOf()

    override fun responseType(): Type {
        return responseType
    }

    override fun adapt(p0: Call<R>): Call<R> {
        return wrapCall(p0)
    }

    fun wrapCall(p0:Call<R>) : Call<R> {
        return object : Call<R> {
            override fun clone(): Call<R> {
                return wrapCall(p0.clone())
            }

            override fun execute(): Response<R> {
                return p0.execute()
            }

            override fun enqueue(callback: Callback<R>) {
                p0.enqueue(callback)
            }

            override fun isExecuted(): Boolean {
                return p0.isExecuted()
            }

            override fun cancel() {
                p0.cancel()
            }

            override fun isCanceled(): Boolean {
                return p0.isCanceled
            }

            override fun request(): Request {
                val or = p0.request()
                val b = Request.Builder()
                    .headers(or.headers())
                    .method(or.method(), or.body())
                    .url(or.url())
                    .tag(MdcContextCopy::class.java, MdcContextCopy(MDC.getCopyOfContextMap()))
                return b.build()
            }

            override fun timeout(): Timeout {
                return p0.timeout()
            }
        }
    }


    class MdcContextCopy(val context: Map<String, String>)
}