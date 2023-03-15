package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-03-15-10:45
 */
@Data
public class FreeDepositOrderVO {
    private String orderId;
    /**
     * 支付状态
     */
    private Integer authStatus;
    /**
     * 押金类型 1：电池，2：租车
     */
    private Integer depositType;

}
