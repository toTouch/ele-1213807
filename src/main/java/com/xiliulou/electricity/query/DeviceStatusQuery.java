package com.xiliulou.electricity.query;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024/10/17 17:34
 */
@Data
public class DeviceStatusQuery {
    
    private String productKey;
    
    private String deviceName;
    
    /***
     * iot平台切换  0. 阿里云 1.华为云 （默认阿里云） 2.自建TCP
     * @return
     */
    private Integer iotConnectMode;
}
