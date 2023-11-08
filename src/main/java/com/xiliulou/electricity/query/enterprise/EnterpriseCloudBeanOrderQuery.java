package com.xiliulou.electricity.query.enterprise;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-09-15-9:35
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnterpriseCloudBeanOrderQuery {
    private Long size;
    private Long offset;

    /**
     * 企业用户id
     */
    private Long uid;
    
    private Long operateUid;

    private Long enterpriseId;
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    /**
     * 租户ID
     */
    private Integer tenantId;
    /**
     * 操作类型 0:赠送,1:后台充值,2:后台扣除
     */
    private Integer type;

    private String orderId;
}
