package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

/**
 * (DivisionAccountConfig)实体类
 *
 * @author Eclair
 * @since 2023-04-23 18:00:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_division_account_config")
public class DivisionAccountConfig {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 分帐配置名称
     */
    private String name;
    /**
     * 分帐层级
     */
    private Integer hierarchy;
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    /**
     * 门店id
     */
    private Long storeId;
    /**
     *
     */
    private BigDecimal operatorRate;

    private BigDecimal operatorRateOther;
    /**
     * 加盟商收益率
     */
    private BigDecimal franchiseeRate;

    private BigDecimal franchiseeRateOther;
    /**
     * 门店收益率
     */
    private BigDecimal storeRate;
    /**
     * 状态（0-启用，1-禁用）
     */
    private Integer status;
    /**
     * 业务类型
     */
    private Integer type;

    private Integer tenantId;
    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;

    private Long createTime;

    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    //业务类型
    public static final Integer TYPE_BATTERY = 1;
    public static final Integer TYPE_CAR = 2;

    //状态
    public static final Integer STATUS_ENABLE = 0;
    public static final Integer STATUS_DISABLE = 1;

    //分帐层级
    public static final Integer HIERARCHY_TWO = 2;
    public static final Integer HIERARCHY_THREE = 3;

}
