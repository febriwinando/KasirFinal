package tech.id.kasir.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL="http://172.15.1.202:8000/api/";
    private static RetrofitClient myClient;
    private final Retrofit retrofit;

    private RetrofitClient(){
        retrofit=new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build();
    }

    final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .readTimeout(120, TimeUnit.SECONDS)
            .connectTimeout(120, TimeUnit.SECONDS)
            .build();

    public static synchronized RetrofitClient getInstance(){
        if (myClient==null){
            myClient=new RetrofitClient();
        }
        return myClient;
    }

    public api_transakasi_data getApi(){
        return retrofit.create(api_transakasi_data.class);

    }
}
