package com.xiliulou.electricity.entity.enterprise;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 企业云豆操作记录表(EnterpriseCloudBeanRecord)实体类
 *
 * @author Eclair
 * @since 2023-09-14 10:16:20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_enterprise_cloud_bean_record")
public class EnterpriseCloudBeanRecord {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * 用户id
     */
    private Long uid;
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
    /**
     * 租户ID
     */
    private Integer tenantId;
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

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
