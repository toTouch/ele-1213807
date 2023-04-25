package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (DivisionAccountCarModel)实体类
 *
 * @author Eclair
 * @since 2023-04-23 18:00:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_division_account_car_model")
public class DivisionAccountCarModel {

    private Long id;
    /**
     * 分帐配置id
     */
    private Long divisionAccountId;
    /**
     * 车辆型号id
     */
    private Long carModelId;

    private Integer tenantId;
    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;

    private Long createTime;

    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
