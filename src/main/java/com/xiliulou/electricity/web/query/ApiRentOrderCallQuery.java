package com.xiliulou.electricity.web.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : eclair
 * @date : 2021/11/11 8:35 上午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiRentOrderCallQuery {
    /**
     * 设备的产品名
     */
    private String productKey;
    /**
     * 设备的编号
     */
    private String deviceName;
    /**
     * 请求硬件命令发送的requestId
     */
    private String requestId;
    /**
     * 第三方订单号
     */
    private String orderId;
    /**
     * 是否异常，false异常，true正常
     */
    private Boolean isException;
    /**
     * 西六楼平台订单状态。
     */
    private String status;
    /**
     * 收到柜机响应消息的时间
     */
    private Long timestamp;
    /**
     * 换电放入仓门号
     */
    private Integer cellNo;
    /**
     * 换电放入电池编号
     */
    private String returnBatteryName;

    /**
     * 异常信息，isException为true时有
     */
    private String msg;

}
