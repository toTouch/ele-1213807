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
    
    private static final String TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJ1aWQiOjE0NDU4MSwicGhvbmUiOiIxMzY1ODUwOTExNSIsImRhdGFUeXBlIjoxLCJ0eXBlIjoxLCJleHAiOjE3MTA4Mjc2NDYsImZ0IjoxNzEwMjIyODQ2OTI5LCJ0ZW5hbnQiOjExNDEsInVzZXJuYW1lIjoi5oKm5p2l5pWw6IO9In0.a_hNsgnSpwpGp6tmsCV-OfJWZwooflcIG22qkbAfm_Z5C_Na26VzLocOSZ_0bK2rvTkLFreCnSqurHA_9sTxvg";
    
    public static void main(String[] args) {
      //upgradeSoftwareVersion();
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
         * 因为离线，无法完成升级、迁移的柜机，五台：
         * N24013000059
         * N24013000083
         * N24013000116
         * N24013000117
         */
        
        
        // K：deviceName
        // V：productKey
        Map<String, String> deviceMap = new HashMap<>();
        deviceMap.put("N24013000002", "a1mqS72fHNi");
        deviceMap.put("N24013000003", "a1mqS72fHNi");
        deviceMap.put("N24013000004", "a1mqS72fHNi");
        deviceMap.put("N24013000005", "a1mqS72fHNi");
        deviceMap.put("N24013000006", "a1mqS72fHNi");
        deviceMap.put("N24013000007", "a1mqS72fHNi");
        deviceMap.put("N24013000008", "a1mqS72fHNi");
        deviceMap.put("N24013000009", "a1mqS72fHNi");
        deviceMap.put("N24013000010", "a1mqS72fHNi");
        deviceMap.put("N24013000011", "a1mqS72fHNi");
        deviceMap.put("N24013000012", "a1mqS72fHNi");
        deviceMap.put("N24013000013", "a1mqS72fHNi");
        deviceMap.put("N24013000014", "a1mqS72fHNi");
        deviceMap.put("N24013000015", "a1mqS72fHNi");
        deviceMap.put("N24013000016", "a1mqS72fHNi");
        deviceMap.put("N24013000017", "a1mqS72fHNi");
        deviceMap.put("N24013000018", "a1mqS72fHNi");
        deviceMap.put("N24013000019", "a1mqS72fHNi");
        deviceMap.put("N24013000020", "a1mqS72fHNi");
        deviceMap.put("N24013000021", "a1mqS72fHNi");
        deviceMap.put("N24013000022", "a1mqS72fHNi");
        deviceMap.put("N24013000023", "a1mqS72fHNi");
        deviceMap.put("N24013000024", "a1mqS72fHNi");
        deviceMap.put("N24013000025", "a1mqS72fHNi");
        deviceMap.put("N24013000026", "a1mqS72fHNi");
        deviceMap.put("N24013000027", "a1mqS72fHNi");
        deviceMap.put("N24013000028", "a1mqS72fHNi");
        deviceMap.put("N24013000029", "a1mqS72fHNi");
        deviceMap.put("N24013000030", "a1mqS72fHNi");
        deviceMap.put("N24013000031", "a1mqS72fHNi");
        deviceMap.put("N24013000032", "a1mqS72fHNi");
        deviceMap.put("N24013000033", "a1mqS72fHNi");
        deviceMap.put("N24013000034", "a1mqS72fHNi");
        deviceMap.put("N24013000035", "a1mqS72fHNi");
        deviceMap.put("N24013000036", "a1mqS72fHNi");
        deviceMap.put("N24013000037", "a1mqS72fHNi");
        deviceMap.put("N24013000038", "a1mqS72fHNi");
        deviceMap.put("N24013000039", "a1mqS72fHNi");
        deviceMap.put("N24013000040", "a1mqS72fHNi");
        deviceMap.put("N24013000041", "a1mqS72fHNi");
        deviceMap.put("N24013000042", "a1mqS72fHNi");
        deviceMap.put("N24013000043", "a1mqS72fHNi");
        deviceMap.put("N24013000044", "a1mqS72fHNi");
        deviceMap.put("N24013000045", "a1mqS72fHNi");
        deviceMap.put("N24013000046", "a1mqS72fHNi");
        deviceMap.put("N24013000047", "a1mqS72fHNi");
        deviceMap.put("N24013000048", "a1mqS72fHNi");
        deviceMap.put("N24013000049", "a1mqS72fHNi");
        deviceMap.put("N24013000050", "a1mqS72fHNi");
        deviceMap.put("N24013000051", "a1mqS72fHNi");
        deviceMap.put("N24013000052", "a1mqS72fHNi");
        deviceMap.put("N24013000053", "a1mqS72fHNi");
        deviceMap.put("N24013000054", "a1mqS72fHNi");
        deviceMap.put("N24013000055", "a1mqS72fHNi");
        deviceMap.put("N24013000056", "a1mqS72fHNi");
        deviceMap.put("N24013000057", "a1mqS72fHNi");
        deviceMap.put("N24013000058", "a1mqS72fHNi");
        deviceMap.put("N24013000060", "a1mqS72fHNi");
        deviceMap.put("N24013000061", "a1mqS72fHNi");
        deviceMap.put("N24013000062", "a1mqS72fHNi");
        deviceMap.put("N24013000063", "a1mqS72fHNi");
        deviceMap.put("N24013000064", "a1mqS72fHNi");
        deviceMap.put("N24013000065", "a1mqS72fHNi");
        deviceMap.put("N24013000066", "a1mqS72fHNi");
        deviceMap.put("N24013000067", "a1mqS72fHNi");
        deviceMap.put("N24013000068", "a1mqS72fHNi");
        deviceMap.put("N24013000069", "a1mqS72fHNi");
        deviceMap.put("N24013000070", "a1mqS72fHNi");
        deviceMap.put("N24013000071", "a1mqS72fHNi");
        deviceMap.put("N24013000072", "a1mqS72fHNi");
        deviceMap.put("N24013000073", "a1mqS72fHNi");
        deviceMap.put("N24013000074", "a1mqS72fHNi");
        deviceMap.put("N24013000075", "a1mqS72fHNi");
        deviceMap.put("N24013000076", "a1mqS72fHNi");
        deviceMap.put("N24013000077", "a1mqS72fHNi");
        deviceMap.put("N24013000078", "a1mqS72fHNi");
        deviceMap.put("N24013000079", "a1mqS72fHNi");
        deviceMap.put("N24013000080", "a1mqS72fHNi");
        deviceMap.put("N24013000081", "a1mqS72fHNi");
        deviceMap.put("N24013000082", "a1mqS72fHNi");
        deviceMap.put("N24013000084", "a1mqS72fHNi");
        deviceMap.put("N24013000085", "a1mqS72fHNi");
        deviceMap.put("N24013000086", "a1mqS72fHNi");
        deviceMap.put("N24013000087", "a1mqS72fHNi");
        deviceMap.put("N24013000088", "a1mqS72fHNi");
        deviceMap.put("N24013000089", "a1mqS72fHNi");
        deviceMap.put("N24013000090", "a1mqS72fHNi");
        deviceMap.put("N24013000091", "a1mqS72fHNi");
        deviceMap.put("N24013000092", "a1mqS72fHNi");
        deviceMap.put("N24013000093", "a1mqS72fHNi");
        deviceMap.put("N24013000094", "a1mqS72fHNi");
        deviceMap.put("N24013000095", "a1mqS72fHNi");
        deviceMap.put("N24013000096", "a1mqS72fHNi");
        deviceMap.put("N24013000097", "a1mqS72fHNi");
        deviceMap.put("N24013000098", "a1mqS72fHNi");
        deviceMap.put("N24013000099", "a1mqS72fHNi");
        deviceMap.put("N24013000100", "a1mqS72fHNi");
        deviceMap.put("N24013000101", "a1mqS72fHNi");
        deviceMap.put("N24013000102", "a1mqS72fHNi");
        deviceMap.put("N24013000103", "a1mqS72fHNi");
        deviceMap.put("N24013000104", "a1mqS72fHNi");
        deviceMap.put("N24013000105", "a1mqS72fHNi");
        deviceMap.put("N24013000106", "a1mqS72fHNi");
        deviceMap.put("N24013000107", "a1mqS72fHNi");
        deviceMap.put("N24013000108", "a1mqS72fHNi");
        deviceMap.put("N24013000109", "a1mqS72fHNi");
        deviceMap.put("N24013000110", "a1mqS72fHNi");
        deviceMap.put("N24013000111", "a1mqS72fHNi");
        deviceMap.put("N24013000112", "a1mqS72fHNi");
        deviceMap.put("N24013000113", "a1mqS72fHNi");
        deviceMap.put("N24013000114", "a1mqS72fHNi");
        deviceMap.put("N24013000115", "a1mqS72fHNi");
        deviceMap.put("N24013000118", "a1mqS72fHNi");
        deviceMap.put("N24013000119", "a1mqS72fHNi");
        deviceMap.put("N24013000120", "a1mqS72fHNi");
        deviceMap.put("N24013000121", "a1mqS72fHNi");
        deviceMap.put("N24013000122", "a1mqS72fHNi");
        deviceMap.put("N24013000123", "a1mqS72fHNi");
        deviceMap.put("N24013000124", "a1mqS72fHNi");
        deviceMap.put("N24013000125", "a1mqS72fHNi");
        deviceMap.put("N24013000126", "a1mqS72fHNi");
        deviceMap.put("N24013000127", "a1mqS72fHNi");
        deviceMap.put("N24013000128", "a1mqS72fHNi");
        deviceMap.put("N24013000129", "a1mqS72fHNi");
        deviceMap.put("N24013000130", "a1mqS72fHNi");
        deviceMap.put("N24013000131", "a1mqS72fHNi");
        deviceMap.put("N24013000132", "a1mqS72fHNi");
        deviceMap.put("N24013000133", "a1mqS72fHNi");
        deviceMap.put("N24013000134", "a1mqS72fHNi");
        deviceMap.put("N24013000135", "a1mqS72fHNi");
        deviceMap.put("N24013000136", "a1mqS72fHNi");
        deviceMap.put("N24013000137", "a1mqS72fHNi");
        deviceMap.put("N24013000138", "a1mqS72fHNi");
        deviceMap.put("N24013000139", "a1mqS72fHNi");
        deviceMap.put("N24013000140", "a1mqS72fHNi");
        deviceMap.put("N24013000141", "a1mqS72fHNi");
        deviceMap.put("N24013000142", "a1mqS72fHNi");
        deviceMap.put("N24013000143", "a1mqS72fHNi");
        deviceMap.put("N24013000144", "a1mqS72fHNi");
        deviceMap.put("N24013000145", "a1mqS72fHNi");
        deviceMap.put("N24013000146", "a1mqS72fHNi");
        deviceMap.put("N24013000147", "a1mqS72fHNi");
        deviceMap.put("N24013000148", "a1mqS72fHNi");
        deviceMap.put("N24013000149", "a1mqS72fHNi");
        deviceMap.put("N24013000150", "a1mqS72fHNi");
        return deviceMap;
    }
    
}
