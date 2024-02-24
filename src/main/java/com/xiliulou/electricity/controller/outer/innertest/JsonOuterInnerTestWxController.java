package com.xiliulou.electricity.controller.outer.innertest;

import com.google.common.collect.Maps;
import com.xiliulou.core.http.resttemplate.service.RestTemplateService;
import com.xiliulou.core.http.resttemplate.service.impl.RestTemplateServiceImpl;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.pay.weixinv3.constant.WechatV3Constant;
import com.xiliulou.pay.weixinv3.service.WechatV3MerchantLoadAndUpdateCertificateService;
import com.xiliulou.pay.weixinv3.util.WechatCredentialsUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author: Ant
 * @className: JsonOuterInnerTestWxController
 * @description: 内部测试接口
 **/
@Slf4j
@RestController
@RequestMapping("/outer/inner/test/wx")
public class JsonOuterInnerTestWxController {
    
    RestTemplateService restTemplateService;
    
    @Resource
    private WechatV3MerchantLoadAndUpdateCertificateService certificateService;
    
    private static String APP_ID = "wx76159ea6aa7a64bc";
    
    private static String MCH_ID = "1300358101";
    
    private static String MCH_SERIAL_NO = "1A3046A07CD03D454F8A05E8491B9490EFFB982F";
    
    private static String OPEN_ID = "oaKjA4ugQKob33prog7JfMbLdmWo";
    
    private static String PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n"
            + "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDAVRDb69Oqicx7Ndk5aX9D9ngDBA7TOWDRt/b2RYcnWS/mN3VUo8XZTcj87t941l4PFwfI/nF2etRfRiDsux37DNRlL6wXVSKQ0+Rjjz1l2IWRZZr4o5IsFnlIzx9aDbx6cU8WEHsxbFnjo/oca7PU0sa7AG9Go7uzPWPcDXNZ33zIXfqHJCVkfxSQXDPmlORl+JH+ypDFw4/MJ54lgq9OL+2Vnk8HtFXEuqjTCUap8kIQtI0cJyVp0B8pFCqnzhb4hZkGxlHWhV8vjhINlqQPgm/Gno/oYebD5i+A4Wf/vXrhCLKRM6xG0aib7EUxeqiejjXD8OHMB1enX2/BlrFjAgMBAAECggEABTZI7qzFX6m8UNp0uImTWVQkjHBftnmnlgdWOfZIY1iXY7W9CD4n7mudNr2CcNPeWKpXG6tJh9xsROixm7i2Z0dBujxAfaFg/mu/lLMdBtWHSRlnybUlEOzK/J4gS2Bz6x2G9yIbNIurI/AdpweEKBBOMmMgoNlvDLxtKFl09sp7m/n/R6KvF5Ocbl3NjGjSb2mbV0CqGNpTIBRLkSz6vKx84EnD99ymd8wEAUHdl/yAAi3G5QwnErkCpHjwo3yohKJOdcaedXe+FNFfj4uVkK9J6Bozy+O68PHJBERGaXwF2Qua5FX7nQs3gVJIPo8SRk3MZT7ru8gQQHOFpGtZgQKBgQDqIcZSiNhuAPQSuSIVLcdFp5eGdsg2HtE0EOOnPagEm4PkwKsFsG7xnBoMa5fVka3P9uCmojHjyqKQBDby3rS8lteja/0Ke29a5guMIjgHJPTCKvuTA23zZ+eXElQOIxbmeQq07hwoWxqr2Hj5VmoHXo21rKWtlVQTCSBl1GbSDwKBgQDSS9acOfoGoWxykJAKzKRF6sT49Ps9msceRJMBpguwObPiFuA49lEp3ClJxSfOO7abzgnxmc6butdz1Me5dCfESQ5g1gSh3abNbn1HhEkem3B5zch63PL3TFJ+MKwZm7kgXEBOXcbNSUnvbLTM4zxUXfkxPN6qmVL5lsiJ71mvbQKBgGP80Ilmz6cK2TE1xwxsZHGmxNsz1wTl3Enz/ITrm9l74LPMZD0lv/uGwskTGeCnOidaM052bf/uBcajRiQqX906PhUYhRP46vFS1ROfeXXznmNdn9qE2Gver9Sgiy0OZzU99AiYt4W0gotpAyDYeqdzBenUNV0QLLuEZkWWk+KZAoGBAJiPXAnKJZBZ2wrhxR2QiDwQrQTO9NUkS/+xT9SKWUBnHBCQ9+lbCFaN2YYi+VWsDDXLq3sSUci4K0Vkv9/SXGcReNTXblTfL/sohMo1NbZ2nYo/t4kqcTjmrZHOTYvmZM8NHbm8XlTfLjv7aM6aq1GpOvZ1ajPpN0DKGoG2miGhAoGBANic7r+JvOzuR4T8Hva1RiUhfOBjGXBVNdPGkD0Y+uIZgetT9HQ1ZzK2KhzfB1VdeuqcQwFhD0pt4RiqszrSzJjso4MEKVpfevPfwOjm6UmZnn0GeOq7GxLvbB82qW314NNypZmOu6UP5E3hTJtfsE/zQThFZBhB/HO8HjgZxarl\n"
            + "-----END PRIVATE KEY-----\n";
    
    
    @GetMapping("/transferBatches")
    public String transferBatches(String batchNo) throws Exception {
        
        // Integer tenantId = 1014;
        
        String url = WechatV3Constant.WE_TRANSFER_BATCH_ORDER_V3;
        
        HashMap<String, Object> params = new HashMap<>();
        params.put("appid", APP_ID);
        params.put("out_batch_no", batchNo);
        params.put("batch_name", "测试商家转账到零钱");
        params.put("batch_remark", "测试商家转账到零钱");
        params.put("total_amount", 2);
        params.put("total_num", 2);
        
        List<Map<String, Object>> subParams = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            HashMap<String, Object> subParamMap = new HashMap<>();
            subParamMap.put("out_detail_no", batchNo + "_detail_" +  String.format("%03d", i));
            subParamMap.put("transfer_amount", 1);
            subParamMap.put("transfer_remark", "推广费");
            subParamMap.put("openid", OPEN_ID);
            subParams.add(subParamMap);
        }
        
        params.put("transfer_detail_list", subParams);
        
        String paramsJson = JsonUtil.toJson(params);
        
        String token = WechatCredentialsUtils.getToken(HttpMethod.POST.toString(), Objects.requireNonNull(HttpUrl.parse(url)), paramsJson, MCH_ID, MCH_SERIAL_NO, getPrivateKey());
        HashMap<String, String> headers = Maps.newHashMap();
        headers.put("Authorization", WechatCredentialsUtils.getSchema() + " " + token);
        headers.put("Accept", "application/json");
        headers.put("Wechatpay-Serial", MCH_SERIAL_NO);
        
        // {"status":200,"headers":{"Server":["nginx"],"Date":["Sat, 24 Feb 2024 06:50:54 GMT"],"Content-Type":["application/json; charset\u003dutf-8"],"Connection":["keep-alive"],"Keep-Alive":["timeout\u003d8"],"Cache-Control":["no-cache, must-revalidate"],"X-Content-Type-Options":["nosniff"],"Request-ID":["08CEA3E6AE0610C20418E0ABB1A80120A75E28CCA505-0"],"Content-Language":["zh-CN"],"Wechatpay-Nonce":["47f77f0a6ce0e9587ad5fb53ee5044d7"],"Wechatpay-Signature":["NnRrU7zsGv091Zqx8b5sDBMNQzf7oFnMEevRvNXHiYIZ+MVxefcFNeJEGB9wHm/aDwFgWwZ2n6l4xpk9+fd0XJebL6mWPOBMMql//Jksx0lZspyV0//4YVIZd6jus2KAGQVAA5UWcQ2QYsPRJs7WHHC4OAZgiB7RIRD0jEGleo7cHiZnePCPpLBu5cgyvIg8lKIsB7V0vABpw8HhWAjpMoO+io42PtC/cICIon1Y5yEQmkMFVSwQjU48sg3qpXyiuYMShKAOajRViljtPmbXYE2NADlXEjYvKIECLzEwJmEJUgTdNcHn2Vs6SeNLpfFWBYVyyT/s3Yi3LxrYaoNMTA\u003d\u003d"],"Wechatpay-Timestamp":["1708757454"],"Wechatpay-Serial":["22C4CE98FA3157B828B4BBF448DCC5986BB4EF3F"],"Wechatpay-Signature-Type":["WECHATPAY2-SHA256-RSA2048"]},"body":"{\"batch_id\":\"131000501099000111125732024022419471051882\",\"batch_status\":\"ACCEPTED\",\"create_time\":\"2024-02-24T14:50:54+08:00\",\"out_batch_no\":\"1234567890TestOutBatchNo\"}"}
        ResponseEntity<String> responseEntity = restTemplateService.postJsonForResponseEntity(url, paramsJson, headers);
        log.info("transferBatches responseEntity is {}", JsonUtil.toJson(responseEntity));
        
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String body = responseEntity.getBody();
            log.info("transferBatches responseEntity body is {}", JsonUtil.toJson(responseEntity));
        }
        
        return "OK";
    }
    
    @PostConstruct
    public void init() {
        restTemplateService = new RestTemplateServiceImpl(new RestTemplate(httpRequestFactory()));
    }
    
    public ClientHttpRequestFactory httpRequestFactory() {
        SSLConnectionSocketFactory socketFactory = null;
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, (X509Certificate[] x509Certificates, String s) -> true);
            socketFactory = new SSLConnectionSocketFactory(builder.build(), new String[] {"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            log.error("初始化免ssl证书失败", e);
        }
        
        RegistryBuilder<ConnectionSocketFactory> registerBuilder = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory());
        if (socketFactory != null) {
            registerBuilder.register("https", socketFactory);
        }
        
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registerBuilder.build());
        connectionManager.setMaxTotal(50);
        connectionManager.setDefaultMaxPerRoute(50);
        RequestConfig requestConfig = RequestConfig.custom()
                //返回数据的超时时间
                .setSocketTimeout(10000)
                //连接上服务器的超时时间
                .setConnectTimeout(2000)
                //从连接池中获取连接的超时时间
                .setConnectionRequestTimeout(1000).build();
        CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).setConnectionManager(connectionManager).build();
        
        return new HttpComponentsClientHttpRequestFactory(httpClient);
        
    }
    
    public static PrivateKey getPrivateKey() throws IOException {
        
        try {
            String privateKey = PRIVATE_KEY.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replaceAll("\\s+", "");
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));
        } catch (NoSuchAlgorithmException var4) {
            log.error("当前Java环境不支持RSA", var4);
        } catch (InvalidKeySpecException var5) {
            log.error("无效的密钥格式", var5);
        } catch (Exception var6) {
            log.error("无效的密钥", var6);
        }
        
        return null;
    }
    
}
