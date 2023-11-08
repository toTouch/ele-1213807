package com.xiliulou.electricity.query.enterprise;

import com.xiliulou.electricity.enums.enterprise.RenewalStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-09-14-10:31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnterpriseInfoQuery {
    private Long size;
    private Long offset;

    /**
     * 主键ID
     */
    private Long id;

    private String name;
    /**
     * 企业站长id
     */
    private Long uid;
    /**
     * 用户电话
     */
    private String phone;
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    /**
     * 企业状态 0:开启,1:关闭
     */
    private Integer status;
    /**
     * 残值回收方式 0:以实际退电日期回收云豆,1:以实际未消耗天数回收云豆
     */
    private Integer recoveryMode;
    /**
     * 企业云豆总数
     */
    private Double totalBeanAmount;
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 自主续费状态 0:不自主续费, 1:自主续费
     * @see RenewalStatusEnum
     */
    private Integer renewalStatus;

    /**
     * 备注
     */
    private String remark;

    private Set<Long> packageIds;

    private Long packageId;

    private Integer packageType;
}
