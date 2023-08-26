package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


/**
 * 换电柜表(TElectricityCarModel)实体类
 *
 * @author makejava
 * @since 2022-06-06 11:00:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_car_model")
public class ElectricityCarModel {
    /**
     * 车辆型号Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 型号名称
     */
    private String name;

    /**
     * 租赁方式
     */
    private String rentType;

    private Integer delFlag;
    /**
     * 租车押金
     */
    private BigDecimal carDeposit;
    /**
     * 其它参数
     */
    private String otherProperties;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;
    /**
     * 门店Id
     */
    private Long storeId;
    /**
     * 加盟商Id
     */
    private Long franchiseeId;

    private Integer tenantId;

    /**
     * 已租数量
     */
    @Builder.Default
    private Integer rentedQuantity = 0;


    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    /**
     * 租赁方式  周租：WEEK_RENT  月租：MONTH_RENT
     */
    public static final String RENT_TYPE_WEEK = "WEEK_RENT";
    public static final String RENT_TYPE_MONTH = "MONTH_RENT";
}
