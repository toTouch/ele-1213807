package com.xiliulou.electricity.utils;

import com.xiliulou.electricity.query.api.ApiRequestQuery;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author : eclair
 * @date : 2021/7/21 2:34 下午
 */
@UtilityClass
@Slf4j
public class SignUtils {
    final String ALGORITHM = "HmacSHA256";


    public String getSignature(ApiRequestQuery dto, String appSecret) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("appId", dto.getAppId());
        map.put("requestTime", dto.getRequestTime());
        map.put("command", dto.getCommand());
        map.put("data", dto.getData());

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
}
