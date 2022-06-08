package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 换电柜表(TElectricityCar)实体类
 *
 * @author makejava
 * @since 2022-06-06 11:00:14
 */
@Data
public class ElectricityCarBindUser {
    /**
     * 用户Id
     */
    @NotNull(message = "用户Id不能为空!", groups = {UpdateGroup.class})
    private Long uid;

    /**
     * 用户Id
     */
    @NotNull(message = "车辆Id不能为空!", groups = {UpdateGroup.class})
    private Integer carId;


}
