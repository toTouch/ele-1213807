package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Integer id;

    /**
     * 型号名称
     */
    @NotBlank(message = "型号名称不能为空", groups = {CreateGroup.class})
    private String name;
    /**
     * 门店Id
     */
    @NotNull(message = "门店不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Long storeId;
    /**
     * 加盟商Id
     */
    @NotNull(message = "加盟商不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Long franchiseeId;

    private Integer delFlag;

    /**
     * 租赁方式
     */
    @NotBlank(message = "租赁方式不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String rentType;

    /**
     * 租赁周期
     */
    //private Integer rentTime;
    /**
     * 租车押金
     */
    @NotNull(message = "租车押金不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private BigDecimal carDeposit;

    /**
     * 车辆型号标签
     */
    private String carModelTag;

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

    private Integer tenantId;


    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    public static final String RENT_TYPE_WEEK = "WEEK_RENT";
    public static final String RENT_TYPE_MONTH = "MONTH_RENT";
}
