package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实名认证审核消息通知
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-11-24-11:37
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationAuditMessageNotify {
    /**
     * 用户名
     */
    private String userName;
    /**
     * 认证时间
     */
    private String authTime;
    /**
     * 业务编码
     */
    private String businessCode;
}