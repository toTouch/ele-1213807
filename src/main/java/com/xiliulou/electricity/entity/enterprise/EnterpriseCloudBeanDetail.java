package com.xiliulou.electricity.entity.enterprise;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 企业云豆详情表(TEnterpriseCloudBeanDetail)实体类
 *
 * @author mxd
 * @since 2025-01-15 19:18:06
 */
@TableName("t_enterprise_cloud_bean_detail")
@Data
public class EnterpriseCloudBeanDetail implements Serializable {
    private static final long serialVersionUID = 151567154558622808L;
    /**
     * 企业id
     */
    private Long enterpriseId;

    /**
     * 已分配云豆数
     */
    private BigDecimal distributableCloudBean;

    /**
     * 已回收云豆数
     */
    private BigDecimal recoveredCloudBean;

    /**
     * 可回收云豆
     */
    private BigDecimal recyclableCloudBean;

    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;

    /**
     * 租户id
     */
    private Integer tenantId;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;
}

