package com.xiliulou.electricity.vo.enterprise;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-09-14-10:33
 */
@Data
public class EnterpriseCloudBeanRecordVO {
    /**
     * 主键ID
     */
    private Long id;
    private String username;
    /**
     * 企业id
     */
    private Long enterpriseId;
    /**
     * 操作类型 0:赠送,1:后台充值,2:后台扣除
     */
    private Integer type;
    /**
     * 云豆数量
     */
    private Double beanAmount;
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    private String franchiseeName;
    /**
     * 租户ID
     */
    private Long tenantId;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;
    /**
     * 备注
     */
    private String remark;
}
