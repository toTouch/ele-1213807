package com.xiliulou.electricity.script;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.query.EleOuterCommandQuery;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: Ant
 * @className: DeviceScript
 * @description:
 **/
public class DeviceScript {
    
    private static final String TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJ1aWQiOjE0NDU4MSwicGhvbmUiOiIxMzY1ODUwOTExNSIsImRhdGFUeXBlIjoxLCJ0eXBlIjoxLCJleHAiOjE3MTA5MjU2MTEsImZ0IjoxNzEwMzIwODExMTUxLCJ0ZW5hbnQiOjExNDEsInVzZXJuYW1lIjoi5oKm5p2l5pWw6IO9In0.vBRTIfsXFpNDrWrfu8RLLdEQjN_q57WYJuCRqTFoMM7OvlBy5JREWgHXqkRnimMHUx1L3-ED_EYjhoDR6XFNPA";
    
    public static void main(String[] args) {
     // upgradeSoftwareVersion();
      changeCloud();
    }
    
    public static void upgradeSoftwareVersion() {
        
        String url = "https://prod-exchange.xiliulou.com/electricityCabinet/admin/electricityCabinet/command";
        AtomicInteger i = new AtomicInteger();
        Map<String, String> deviceMap = getDeviceMap();
        deviceMap.forEach((k, v) -> {
            String sessionId = UUID.randomUUID().toString().replace("-", "");
            EleOuterCommandQuery eleOuterCommandQuery = new EleOuterCommandQuery();
            eleOuterCommandQuery.setCommand("cupboard_update_application");
            eleOuterCommandQuery.setDeviceName(k);
            eleOuterCommandQuery.setProductKey(v);
            eleOuterCommandQuery.setSessionId(sessionId);
            
            HttpRequest post = HttpUtil.createPost(url);
            Map<String, String> headers = new HashMap<>();
            headers.put("Xll-Sin-Client-Id", "xiliulou-ele");
            headers.put("xll-sin-token", TOKEN);
            headers.put("Api-V", "v2");
            post.addHeaders(headers);
            post.body(JsonUtil.toJson(eleOuterCommandQuery));
            HttpResponse execute = post.execute();
            System.err.println("sessionId is : " + sessionId + ", status is : " + execute.getStatus());
            System.err.println("sessionId is : " + sessionId + ", body is : " + execute.body());
            if (execute.getStatus() == 200) {
                i.getAndIncrement();
            }
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.err.println("成功数：" + i);
        });
    }
    
    public static void changeCloud() {
        
        String url = "https://prod-exchange.xiliulou.com/electricityCabinet/admin/electricityCabinet/command";
        
        Map<String, String> deviceMap = getDeviceMap();
        deviceMap.forEach((k, v) -> {
            String sessionId = UUID.randomUUID().toString().replace("-", "");
            EleOuterCommandQuery eleOuterCommandQuery = new EleOuterCommandQuery();
            eleOuterCommandQuery.setCommand("other_setting");
            eleOuterCommandQuery.setDeviceName(k);
            eleOuterCommandQuery.setProductKey(v);
            eleOuterCommandQuery.setSessionId(sessionId);
            Map<String, Object> data = new HashMap<>();
            data.put("iotConnectMode", "1");
            data.put("apiAddress", "https://prod-exchange.kteshd.com");
            data.put("qrAddressV2", "https://exchange.kteshd.com");
            eleOuterCommandQuery.setData(data);
            
            HttpRequest post = HttpUtil.createPost(url);
            Map<String, String> headers = new HashMap<>();
            headers.put("Xll-Sin-Client-Id", "xiliulou-ele");
            headers.put("xll-sin-token", TOKEN);
            headers.put("Api-V", "v2");
            post.addHeaders(headers);
            post.body(JsonUtil.toJson(eleOuterCommandQuery));
            HttpResponse execute = post.execute();
            System.err.println("sessionId is : " + sessionId + ", status is : " + execute.getStatus());
            System.err.println("sessionId is : " + sessionId + ", body is : " + execute.body());
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    public static Map<String, String> getDeviceMap() {
        /**
         * 因为离线，无法完成升级、迁移的柜机：
         * N24013000116
         * N24013000117
         */
        
        
        // K：deviceName
        // V：productKey
        Map<String, String> deviceMap = new HashMap<>();
        deviceMap.put("N24013000059", "a1mqS72fHNi");
        deviceMap.put("N24013000083", "a1mqS72fHNi");
        return deviceMap;
    }
    
}
