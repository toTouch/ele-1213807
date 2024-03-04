package com.xiliulou.electricity.script;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.query.EleOuterCommandQuery;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author: Ant
 * @className: DeviceScript
 * @description:
 **/
public class DeviceScript {
    
    public static void main(String[] args) {
        
        String token = "";
        
        String url = "https://yl.xiliulou.com/electricityCabinet/admin/electricityCabinet/command";
        
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
            data.put("apiAddress", "https://yl.xiliulou.com");
            data.put("qrAddress", "https://exchange.xiliulou.com");
            eleOuterCommandQuery.setData(data);
            
            HttpRequest post = HttpUtil.createPost(url);
            Map<String, String> headers = new HashMap<>();
            headers.put("Xll-Sin-Client-Id", "xiliulou-ele");
            headers.put("xll-sin-token", token);
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
        // K：deviceName
        // V：productKey
        Map<String, String> deviceMap = new HashMap<>();
        deviceMap.put("", "");
        return deviceMap;
    }
    
}
