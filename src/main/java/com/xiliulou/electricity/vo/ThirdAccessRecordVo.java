package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author : eclair
 * @date : 2021/8/30 9:00 上午
 */
@Data
public class ThirdAccessRecordVo {
    /**
     * 请求Id
     */
    private String requestId;
    /**
     * 请求时间
     */
    private Long requestTime;
    /**
     * 柜机响应时间
     */
    private Long responseTime;

    /**
     * 操作类型
     */
    private String operateType;
    /**
     * 额外信息
     */
    private String attrMsg;
}
