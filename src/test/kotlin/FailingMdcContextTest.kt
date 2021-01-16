import kotlinx.coroutines.runBlocking
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
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.*

@RunWith(JUnit4::class)
class FailingMdcContextTest {
    @Rule
    @JvmField
    val mockWebServer = MockWebServer()
    val log = LoggerFactory.getLogger(this.javaClass)
    val old = System.out
    val baos = ByteArrayOutputStream()
    val expectedTxId = UUID.randomUUID().toString()

    @Before
    fun initServerResponse() {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse()
                    .setResponseCode(200)
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

    @Test
    fun callClientLogEventsShouldContainMdcId() {
        val ps = PrintStream(baos)
        System.setOut(ps)

        val client = createRetrofitClient(mockWebServer)
        runBlocking {
            MDC.putCloseable("txId", expectedTxId).run {
                log.info("Message")
                client.getSomethingWithCall().execute()
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
            .build()
            .create(SampleClient::class.java)

    }

    fun createOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor { message ->
            log.info(message)
        }
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    interface SampleClient {
        @GET("/api/call")
        suspend fun getSomething(): ResponseBody

        @GET("/api/call")
        fun getSomethingWithCall() : Call<ResponseBody>
    }
}