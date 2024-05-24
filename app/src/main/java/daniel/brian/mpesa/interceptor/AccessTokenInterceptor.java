package daniel.brian.mpesa.interceptor;


import android.util.Base64;

import androidx.annotation.NonNull;


import java.io.IOException;

import daniel.brian.mpesa.BuildConfig;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/*
- We're going to encode the consumer key and
secret to create the auth token and pass it as a
header for our JSON request
*/
public class AccessTokenInterceptor implements Interceptor {
    public AccessTokenInterceptor(){

    }


    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        String keys = BuildConfig.CONSUMER_KEY + ":" + BuildConfig.CONSUMER_SECRET;

        String encodedKeys = Base64.encodeToString(keys.getBytes(),Base64.NO_WRAP);

        Request request = chain.request().newBuilder()
                .addHeader("Authorization","Basic " + encodedKeys)
                .build();

        return chain.proceed(request);
    }
}
