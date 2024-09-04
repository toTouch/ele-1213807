package com.xiliulou.electricity.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-08-14-18:10
 */
@Slf4j
@Configuration
public class DeviceReportRestTemplateConfig {
    
    @Value("${remote.connectTimeout:2000}")
    private int connectTimeout;
    @Value("${remote.readTimeout:5000}")
    private int readTimeout;
    @Value("${remote.maxTotalConnect:100}")
    private int maxTotalConnect;
    @Value("${remote.maxConnectPerRoute:100}")
    private int maxConnectPerRoute;
    
    public ClientHttpRequestFactory httpRequestFactory() {
        if (this.maxTotalConnect <= 0) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(this.connectTimeout);
            factory.setReadTimeout(this.readTimeout);
            return factory;
        }
        
        SSLConnectionSocketFactory socketFactory = null;
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, (X509Certificate[] x509Certificates, String s) -> true);
            socketFactory = new SSLConnectionSocketFactory(builder.build(), new String[]{"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            log.error("初始化免ssl证书失败", e);
        }
        
        RegistryBuilder<ConnectionSocketFactory> registerBuilder = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory());
        if (socketFactory != null) {
            registerBuilder.register("https", socketFactory);
        }
        
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registerBuilder.build());
        connectionManager.setMaxTotal(this.maxTotalConnect);
        connectionManager.setDefaultMaxPerRoute(this.maxConnectPerRoute);
        
        RequestConfig requestConfig = RequestConfig.custom()
                //返回数据的超时时间
                .setSocketTimeout(this.readTimeout)
                //连接上服务器的超时时间
                .setConnectTimeout(this.connectTimeout)
                //从连接池中获取连接的超时时间
                .setConnectionRequestTimeout(1000)
                .build();
        
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .build();
        
        return new HttpComponentsClientHttpRequestFactory(httpClient);
        
    }
    
    @Bean(value = "deviceReportRestTemplate")
    public RestTemplate getRestTemplate() {
        return new RestTemplate(httpRequestFactory());
    }
}
