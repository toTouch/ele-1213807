package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-08-10-15:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TenantNotifyMailVO {
    private Long id;
    /**
     * 通知邮箱json
     */
    private String mail;

    private Long tenantId;

    private String tenantName;

    private Long createTime;

    private Long updateTime;
}
