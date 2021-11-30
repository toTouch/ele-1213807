package com.xiliulou.electricity.service.retrofilt.api;

import com.xiliulou.core.http.retrofit.ApiDynamicUrlInterceptor;
import com.xiliulou.core.http.retrofit.LogInterceptor;
import com.xiliulou.core.http.retrofit.RetrofitEasyX509TrustManager;
import com.xiliulou.core.http.retrofit.RetryInterceptor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * @author : eclair
 * @date : 2021/8/4 5:16 下午
 */
@Service
@Slf4j
public class RetrofitThirdApiService {
    @Autowired
    EleThirdApiDynamicUrlServiceImpl eleThirdApiDynamicUrlService;

    Retrofit retrofit;

    @PostConstruct
    public void init() throws NoSuchAlgorithmException, KeyStoreException {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.queuedCallsCount();
        dispatcher.setMaxRequestsPerHost(20);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .sslSocketFactory(createEasySSLContext().getSocketFactory(), new RetrofitEasyX509TrustManager(null))
                .connectTimeout(2, TimeUnit.SECONDS).
                        readTimeout(5, TimeUnit.SECONDS).
                        writeTimeout(5, TimeUnit.SECONDS)
                .dispatcher(dispatcher)
                .addInterceptor(new ApiDynamicUrlInterceptor(eleThirdApiDynamicUrlService))
                .addInterceptor(new RetryInterceptor(2))
                .addInterceptor(new LogInterceptor())
                .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))
                .build();


        retrofit = new Retrofit.Builder()
                .client(httpClient)
                .baseUrl("http://localhost")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

    }

    private static SSLContext createEasySSLContext() {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, null, null);
            return context;
        } catch (Exception e) {
            log.error("createEasySSlContext error!", e);
        }
        return null;
    }

    public <T>  T getRetrofitService(Class<T> clazz) {
        return retrofit.create(clazz);
    }
}
