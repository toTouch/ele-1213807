package com.xiliulou.electricity.service.alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipayEncrypt;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-07-02-18:06
 */
@Slf4j
public class AliPayAuthTest {
    
    private static final String SIGN_TYPE = "RSA2";
    
    private static final String CHARSET = "UTF-8";
    
    private static final String ENCRYPT_TYPE = "AES";
    
    //支付宝公钥
    private static final String SIGN_VERI_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjAvf3qdHU+2h/2RvPsONumsEfThA/x4gL65UHrDISNyOf4kWITKiH9bahLXF5Y0v/NZS9tTUND7bq7EK2Qi7GJz6thcFyVe2hUgKMbOdfnZTXMmgVR8FOIbyoclhRZd3TSJRN2DvfZ6EW0uz7dDk9QUsKEshN759czM1wlGWBugrUXY7HEF4HJoHJ/5QuRBaTN36VFiPGPzjtvpvW3Mo9632a6uj4co6jEzXg2W8RGLyiRCWRUFBs9PRJGf2BccvZjanV3AjPGybpyOxEeJVQTziqcyAYLYCYwZE4CQkYKbpksOmLOfWZkR2qawp97K6dyYX0IjV/A5uxUrRBjBJwQIDAQAB";
    
    //应用私钥
    private static final String PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCrRFmF7E6HnOCiBCr084z9RFBSTXgpMKogP3weZAADEssUQH7uFBPKHez1K04UUUfnhRRqmsuz9l4D/3/jXFmdsZJROtqEHR4+uh8efEFsilKoI4jyCGfP6/gJzy7yDsS/LU6TqoODkhY5NWtmQiOyVDNgifb994YvcZq2Hs4CMJ4NxZrj89MnieUWVTwtig1vuE3wBhKX4/fTZJsX+ATWpfd7pR5evO1rjCmObZmeJ0wh/jRC0/eX7YS4ml2FPfPSRl3LQYWO+dRPgQGxRZCeXnn2EKTjtz/efKjP12miXdF7htsX2bT+J6HRlomzq2pYDLkrHJDa/xrYXawcU09VAgMBAAECggEAVBh3rN7foI9TbbqGLUj5zdKhbghEHFWc88C4fWO07c1okkUpDlYlcXVISQo+iJNwryoVYFMp+u/aMjRe45ERH/F1WxV+/qgdlcdmSF3S8izpcU4hjFa8QsgnPwnQj2LZENZ6Yt+zPAObjfPBDLElPgdCQwD7mrDT3q/1u79cgI327R53scI+fDyaTpq1/aKLGtj4k0reIYAD/FMQUaNB49z14Zwt6WaHuFSWT3D6sx3KuZVRGjUKUTSjGlS7AsU4WyHAxHZnitP48zga1pimtVhPDpZ+OzrjxHFyqNoPFLWDelmHBUewV/8ons4q32SQ5/9yULfW5CYvllWTcGpr8QKBgQDys5IvmPeS2AlLfzNnTr0UcZDyDHAxCp2mSVMZHdqcv02FAtPejEvW/hdOeqgJwDKqp9Sazqy03BNiTrVvsDxcAZNIwP1X//WFL6h+31AijfxE7hVJNHfJSUfRnmPjLT9BzDeJ9GUoTPvoaqTk4kZ+Z2QrH9NeVxTQR/vtNIjerwKBgQC0psBIw+vs0wacHEuDJrY/HX8wg88FMD6rblP6CDcGudUr76cG0WuCkH0r/3GZ486GwKVMUsmz5zuBFt/kANqOO/uy0qZYgdjHmuvHNr1qIlanJmwX7Ia36qr/x0ppu6FHzV9oUE0kaslkdMuzKCSLnQeuZ50EV9WkO0BZ9A8TOwKBgALdyA4z2kirsIBpwiuoLGd/Z9zT9Mc/ftkl6ItVZO2Q/NNjUyk/su2ZFqFgpXdoA7EsRkCFzFheeQQiNdZZ2HylsB2d2eAeL8Ig6/aDoKin0KDnxuyUaA3Chcyd+EQIlsSqKsXAUymErzzxdX0WhwqbIf24ZICqup4zG3CTvEIVAoGBALFE6G7/AqX0Ngo+ocLi2/d3RHYhAaa/vt+Odg1mvkh1Vr+0fZxtKCiJDKt+EMXIC8OjixEoNBG7mGKGRdGBHPZx2f2SQ/WaBVVpqnBkQN7DL3D6fRvE2DXlq0MvFtBGdG73EuZT1j8kItfW3ITDoYj24LC9sBCw+E4ebnlWyuw9AoGAX1vbzP2lHcHIOVombcDmiLN/uddNBS9wis5isncPZVwpF2vPGU6voHOJYrTvRwnX8YsMjfAR83FW+KmErVrpeN6/xebh1xMCxRJHQWeTzhRlxwIdrzQlSz2e1Xo9UmEyuL0yn9VvIyn1ephmhWxgIWeMHJajC3cM+PyQj5vXaYY=";
    
    //加解密密钥
    private static final String DECRYPT_KEY = "qnyJJ6MM7oN7dLp22MbAww==";
    
    private static final String APP_ID = "2021004144647049";
    
    
    /**
     * 解密支付宝小程序登录报文，获取用户手机号
     */
    @Test
    public void testDecryptAliPayUserPhone() {
        //1. 获取验签和解密所需要的参数
        String content = "x/oZM5xhpEWmzfNSGwTh9rOfir94Em05F2T5I+6wqh7z53UYvzL+w8+sc4C9I8N73GppJlM/KK0ykgtu25xETw==";
        String sign = "DBcCx/R+W3MZlt81DYDpr1KGy5zI++LrZTuM+bxH9pAOkiufr03kNPFfyoDzHUFzyCFPsEzDSAkW4INHw0G8hfzpA2h/AvugPK3cm8nmbK/ZXCzLDqBlOkeKR/e9rkRyFVxmJMB/PAePev+Dj833jIGYyXzkMJeUWNBwIr16TansWy4mjf58A6U39pu4eFTjfPf1BdECVrKpTXhcfsfDbnVfRVAXA+pqcKvsfkgDbk0ABir8+Gl92nVCEmcYIqM/hwyBe05wo3npnuUhxOOoL09BcGA+KeVDkiSFEbhEoQ0FPTfHVuYnMMmyhCNOWKX+CEXtiqieohutYzDmQONmRA==";
        
        //判断是否为加密内容
        boolean isDataEncrypted = !content.startsWith("{");
        //2. 验签
        String signContent = content;
        if (isDataEncrypted) {
            signContent = "\"" + signContent + "\"";
        }
        
        String plainData = content;
        try {
            if (!AlipaySignature.rsaCheck(signContent, sign, SIGN_VERI_KEY, CHARSET, SIGN_TYPE)) {
                log.error("验签不通过（异常或者报文被篡改），终止流程");
                return;
            }
            
            //3. 解密
            if (isDataEncrypted) {
                plainData = AlipayEncrypt.decryptContent(content, ENCRYPT_TYPE, DECRYPT_KEY, CHARSET);
            }
        } catch (AlipayApiException e) {
            log.error("验签异常", e);
        }
        
        log.error("用户手机号解密结果={}", plainData);
    }
    
    /**
     * 解密支付宝小程序报文，获取openId
     */
    @Test
    public void testDecryptAliPayAuthData() {
        try {
            // 初始化SDK
            AlipayClient alipayClient = new DefaultAlipayClient(getAlipayConfig());
            
            // 构造请求参数以调用接口
            AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
            
            // 设置刷新令牌
            // request.setRefreshToken("201208134b203fe6c11548bcabd8da5bb087a83b");
            
            // 设置授权码
            request.setCode("f87c7722f0ac4e8896ca6eb50cdcWX23");
            
            // 设置授权方式
            request.setGrantType("authorization_code");
            
            AlipaySystemOauthTokenResponse response = alipayClient.execute(request);
            log.info("acquire openId result={}", response.getBody());
            
            if (response.isSuccess()) {
                log.info("调用成功");
            } else {
                log.error("调用失败");
            }
        } catch (AlipayApiException e) {
            log.error("获取openId失败", e);
        }
    }
    
    private static AlipayConfig getAlipayConfig() {
        //        String privateKey  = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCrRFmF7E6HnOCiBCr084z9RFBSTXgpMKogP3weZAADEssUQH7uFBPKHez1K04UUUfnhRRqmsuz9l4D/3/jXFmdsZJROtqEHR4+uh8efEFsilKoI4jyCGfP6/gJzy7yDsS/LU6TqoODkhY5NWtmQiOyVDNgifb994YvcZq2Hs4CMJ4NxZrj89MnieUWVTwtig1vuE3wBhKX4/fTZJsX+ATWpfd7pR5evO1rjCmObZmeJ0wh/jRC0/eX7YS4ml2FPfPSRl3LQYWO+dRPgQGxRZCeXnn2EKTjtz/efKjP12miXdF7htsX2bT+J6HRlomzq2pYDLkrHJDa/xrYXawcU09VAgMBAAECggEAVBh3rN7foI9TbbqGLUj5zdKhbghEHFWc88C4fWO07c1okkUpDlYlcXVISQo+iJNwryoVYFMp+u/aMjRe45ERH/F1WxV+/qgdlcdmSF3S8izpcU4hjFa8QsgnPwnQj2LZENZ6Yt+zPAObjfPBDLElPgdCQwD7mrDT3q/1u79cgI327R53scI+fDyaTpq1/aKLGtj4k0reIYAD/FMQUaNB49z14Zwt6WaHuFSWT3D6sx3KuZVRGjUKUTSjGlS7AsU4WyHAxHZnitP48zga1pimtVhPDpZ+OzrjxHFyqNoPFLWDelmHBUewV/8ons4q32SQ5/9yULfW5CYvllWTcGpr8QKBgQDys5IvmPeS2AlLfzNnTr0UcZDyDHAxCp2mSVMZHdqcv02FAtPejEvW/hdOeqgJwDKqp9Sazqy03BNiTrVvsDxcAZNIwP1X//WFL6h+31AijfxE7hVJNHfJSUfRnmPjLT9BzDeJ9GUoTPvoaqTk4kZ+Z2QrH9NeVxTQR/vtNIjerwKBgQC0psBIw+vs0wacHEuDJrY/HX8wg88FMD6rblP6CDcGudUr76cG0WuCkH0r/3GZ486GwKVMUsmz5zuBFt/kANqOO/uy0qZYgdjHmuvHNr1qIlanJmwX7Ia36qr/x0ppu6FHzV9oUE0kaslkdMuzKCSLnQeuZ50EV9WkO0BZ9A8TOwKBgALdyA4z2kirsIBpwiuoLGd/Z9zT9Mc/ftkl6ItVZO2Q/NNjUyk/su2ZFqFgpXdoA7EsRkCFzFheeQQiNdZZ2HylsB2d2eAeL8Ig6/aDoKin0KDnxuyUaA3Chcyd+EQIlsSqKsXAUymErzzxdX0WhwqbIf24ZICqup4zG3CTvEIVAoGBALFE6G7/AqX0Ngo+ocLi2/d3RHYhAaa/vt+Odg1mvkh1Vr+0fZxtKCiJDKt+EMXIC8OjixEoNBG7mGKGRdGBHPZx2f2SQ/WaBVVpqnBkQN7DL3D6fRvE2DXlq0MvFtBGdG73EuZT1j8kItfW3ITDoYj24LC9sBCw+E4ebnlWyuw9AoGAX1vbzP2lHcHIOVombcDmiLN/uddNBS9wis5isncPZVwpF2vPGU6voHOJYrTvRwnX8YsMjfAR83FW+KmErVrpeN6/xebh1xMCxRJHQWeTzhRlxwIdrzQlSz2e1Xo9UmEyuL0yn9VvIyn1ephmhWxgIWeMHJajC3cM+PyQj5vXaYY=";
        //        String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjAvf3qdHU+2h/2RvPsONumsEfThA/x4gL65UHrDISNyOf4kWITKiH9bahLXF5Y0v/NZS9tTUND7bq7EK2Qi7GJz6thcFyVe2hUgKMbOdfnZTXMmgVR8FOIbyoclhRZd3TSJRN2DvfZ6EW0uz7dDk9QUsKEshN759czM1wlGWBugrUXY7HEF4HJoHJ/5QuRBaTN36VFiPGPzjtvpvW3Mo9632a6uj4co6jEzXg2W8RGLyiRCWRUFBs9PRJGf2BccvZjanV3AjPGybpyOxEeJVQTziqcyAYLYCYwZE4CQkYKbpksOmLOfWZkR2qawp97K6dyYX0IjV/A5uxUrRBjBJwQIDAQAB";
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl("https://openapi.alipay.com/gateway.do");
        alipayConfig.setAppId(APP_ID);
        alipayConfig.setPrivateKey(PRIVATE_KEY);
        alipayConfig.setFormat("json");
        alipayConfig.setAlipayPublicKey(SIGN_VERI_KEY);
        alipayConfig.setCharset(CHARSET);
        alipayConfig.setSignType(SIGN_TYPE);
        return alipayConfig;
    }
    
}
