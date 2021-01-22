import io.reactivex.Flowable
import io.reactivex.plugins.RxJavaPlugins
import kotlinx.coroutines.*
import kotlinx.coroutines.slf4j.MDCContext
import mu.KotlinLogging
import mu.withLoggingContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.slf4j.MDC
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.lang.Runnable
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.coroutines.*

@RunWith(JUnit4::class)
class FailingMdcContextTest {
    @Rule
    @JvmField
    val mockWebServer = MockWebServer()
    val log = KotlinLogging.logger {}
    val old = System.out
    val baos = ByteArrayOutputStream()

    @Before
    fun initServerResponse() {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse()
                    .setResponseCode(200)
                        .setBody("""
                            {"bar": "baz"}
                        """.trimIndent())
            }
        }
    }

    @After
    fun clean() {
        System.out.flush()
        System.setOut(old)
        println(baos)
    }

    @Test
    fun suspendClientLogEventsShouldContainMdcId() {
        val ps = PrintStream(baos)
        System.setOut(ps)
        val expectedTxId = UUID.randomUUID().toString()

        val client = createRetrofitClient(mockWebServer)
        runBlocking {
            MDC.putCloseable("txId", expectedTxId).run {
                log.info("Message")
                client.getSomething()
            }
        }
        val txIds = baos.toString()
            .split('\n')
            .filter { it.contains("tx-id") }
            .map { message ->
                message.split(" ").first { it.contains("tx-id") }
            }
            .map {
                it.replace("tx-id=", "")
            }
        txIds.forEach {
            Assert.assertEquals(expectedTxId, it)
        }
    }



    fun createRetrofitClient(mockServer: MockWebServer): SampleClient {
        return Retrofit.Builder()
            .baseUrl(mockServer.url("/"))
            .client(
                    createOkHttpClient()
            )
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(MdcCallAdapterFactory())
            .build()
            .create(SampleClient::class.java)

    }

    fun createOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor { message ->
            log.underlyingLogger.info(message)

        }
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)
        return OkHttpClient.Builder()
            .addInterceptor(logging)
                .addInterceptor(object : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                        return chain.proceed(chain.request())
                    }

                })
            .build()
    }

    interface SampleClient {
        @GET("/api/call")
        suspend fun getSomething(): Response<Foo>
    }

    interface RxSampleClient {
        @GET("/api/call")
        fun getSomethingWithRx() : Flowable<ResponseBody>
    }

    data class Foo(val bar:String)
}