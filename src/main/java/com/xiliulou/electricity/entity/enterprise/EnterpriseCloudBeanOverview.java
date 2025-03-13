package com.xiliulou.electricity.entity.enterprise;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 企业云豆总览表(TEnterpriseCloudBeanOverview)实体类
 *
 * @author makejava
 * @since 2025-01-15 19:56:24
 */
@TableName("t_enterprise_cloud_bean_overview")
@Data
public class EnterpriseCloudBeanOverview implements Serializable {
    private static final long serialVersionUID = 456542852607479416L;
    /**
     * 企业id
     */
    private Long enterpriseId;


    /**
     * 已分配云豆数
     */
    private BigDecimal allocationCloudBean;

    /**
     * 已分配套餐数
     */
    private Integer allocationPackageCount;

    /**
     * 已分配人数
     */
    private Integer allocationUserCount;

    /**
     * 已回收云豆数
     */
    private BigDecimal recycleCloudBean;

    /**
     * 已回收套餐数
     */
    private Integer recyclePackageCount;

    /**
     * 已回收用户数
     */
    private Integer recycleUserCount;

    /**
     * 可回收云豆数
     */
    private BigDecimal canRecycleCloudBean;

    /**
     * 可回收套餐数
     */
    private Integer canPackageCount;

    /**
     * 可回收用户数
     */
    private Integer canRecycleUserCount;

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

