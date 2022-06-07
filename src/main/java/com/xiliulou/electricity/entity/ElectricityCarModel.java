package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


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

    private Integer delFlag;

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
}
