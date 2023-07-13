package com.xiliulou.electricity.utils;

import com.xiliulou.electricity.query.api.ApiRequestQuery;
import com.xiliulou.esign.exception.EsignException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author : eclair
 * @date : 2021/7/21 2:34 下午
 */
@UtilityClass
@Slf4j
public class SignUtils {
    final String ALGORITHM = "HmacSHA256";

    final String CHARSETS_UTF8 = "UTF-8";
    
    
    public String getSignature(String appId,Long requestTime,String requestId,String version, String appSecret) throws Exception {        Map<String, Object> map = new HashMap<>();
        map.put("appId", appId);
        map.put("requestTime", requestTime);
        map.put("requestId", requestId);
        map.put("version", version);
    
        // 先将参数以其参数名的字典序升序进行排序
        Map<String, Object> sortedParams = new TreeMap<>(map);
        Set<Map.Entry<String, Object>> entrySet = sortedParams.entrySet();
    
        // 遍历排序后的字典，将所有参数按"key=value"格式拼接在一起
        StringBuilder stringToSign = new StringBuilder();
        for (Map.Entry<String, Object> param : entrySet) {
            stringToSign.append(param.getKey()).append("=").append(param.getValue()).append(",");
        }
        stringToSign.deleteCharAt(stringToSign.length() - 1);
        return calSignature(appSecret, stringToSign.toString());
    }


    private String calSignature(String appSecret, String dataToSign) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Mac sha256HMAC = Mac.getInstance(ALGORITHM);
        sha256HMAC.init(secretKeySpec);
        byte[] hmacResult = sha256HMAC.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8));
        Base64.Encoder encoder = Base64.getUrlEncoder();
        byte[] base64Result = encoder.encode(hmacResult);
        return new String(base64Result);
    }

    /**
     * 获取请求body
     * @param request
     * @return
     */
    public String getRequestBody(HttpServletRequest request) {
        // 请求内容RequestBody
        String reqBody = null;
        int contentLength = request.getContentLength();
        if (contentLength < 0) {
            return null;
        }
        byte buffer[] = new byte[contentLength];
        try {
            for (int i = 0; i < contentLength;) {
                int readlen = request.getInputStream().read(buffer, i, contentLength - i);
                if (readlen == -1) {
                    break;
                }
                i += readlen;
            }
            reqBody = new String(buffer, CHARSETS_UTF8);

        } catch (IOException e) {
           log.error("get request body info error： {}", e);
        }
        return reqBody;
    }

    /**
     * 获取query请求字符串
     * @param request
     * @return
     */
    public  String getRequestQueryStr(HttpServletRequest request) {
        //对 Query 参数按照字典对 Key 进行排序后,按照value1+value2方法拼接
        //转换一下数据类型并排序
        List<String> req_List= new ArrayList();
        Enumeration<String> reqEnu =request.getParameterNames();
        while (reqEnu.hasMoreElements()){
            req_List.add(reqEnu.nextElement());
        }
        Collections.sort(req_List);
        String requestQuery = "";
        for (String key : req_List) {
            String value = request.getParameter(key);
            requestQuery += value == null ? "" : value;
        }
        log.debug("get request query str is：" + requestQuery);
        return  requestQuery;
    }

    public static String getSignature(String data, String key) {
        Mac mac = null;
        try {
            mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(CHARSETS_UTF8), ALGORITHM);
            mac.init(secretKey);
            mac.update(data.getBytes(CHARSETS_UTF8));
        } catch (Exception e) {
            log.error("encryption signature info error: {}, data: {}, key: {}", e, data, key);
            throw new EsignException("加密签名信息异常！", e);
        }
        return byte2hex(mac.doFinal());
    }

    /***
     * 将byte[]转成16进制字符串
     *
     * @param data
     *
     * @return 16进制字符串
     */
    public static String byte2hex(byte[] data) {
        StringBuilder hash = new StringBuilder();
        String stmp;
        for (int n = 0; data != null && n < data.length; n++) {
            stmp = Integer.toHexString(data[n] & 0XFF);
            if (stmp.length() == 1)
                hash.append('0');
            hash.append(stmp);
        }
        return hash.toString();
    }

}
