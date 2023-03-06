package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-24-9:57
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FreeDepositOrderQuery {
    private Long size;
    private Long offset;

    private Long startTime;
    private Long endTime;
    private Integer tenantId;

    private String orderId;

    /**
     * 支付宝绑定的手机号
     */
    private String phone;
    /**
     * 身份征号
     */
    private String idCard;
    /**
     * 用户真实姓名
     */
    private String realName;

    /**
     * 授权免押的状态
     */
    private Integer authStatus;
    /**
     * 支付状态
     */
    private Integer payStatus;

    /**
     * 押金类型 1：电池，2：租车
     */
    private Integer depositType;
}
