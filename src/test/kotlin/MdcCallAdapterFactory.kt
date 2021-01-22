import io.reactivex.*
import org.slf4j.MDC
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class MdcCallAdapterFactory: CallAdapter.Factory() {
    override fun get(responseType: Type, p1: Array<Annotation>, p2: Retrofit): MdcCallAdapter<*> {
        check(responseType is ParameterizedType) {
            "return type must be parameterized as Call<NetworkResponse<<Foo>> or Call<NetworkResponse<out Foo>>"
        }

        return MdcCallAdapter<Any>(getParameterUpperBound(0, responseType))
    }

}