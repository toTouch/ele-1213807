package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 退租审核
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-09-06-10:54
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentRefundAuditMessageNotify {
    /**
     * 用户名
     */
    private String userName;
    /**
     * 申请时间
     */
    private String applyTime;
    /**
     * 业务编码
     */
    private String businessCode;
}
