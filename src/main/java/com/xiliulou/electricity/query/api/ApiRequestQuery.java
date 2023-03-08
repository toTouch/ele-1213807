package com.xiliulou.electricity.query.api;

import lombok.Data;

/**
 * @author: Miss.Li
 * @Date: 2021/11/5 10:19
 * @Description:
 */
@Data
public class ApiRequestQuery {
    /**
     * 版本号
     */
    private String version;
    /**
     */
    private Long requestTime;
    /**
     * 请求Id
     */
    private String requestId;
    /**
     * appId
     */
    private String appId;
    /**
     * 生成的签名
     */
    private String sign;
    
    /**
     * 附带的数据
     */
    private String data;

}
