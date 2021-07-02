package com.xiliulou.electricity.query;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 订单表(TElectricityCabinetOrder)实体类
 *
 * @author makejava
 * @since 2020-11-26 16:00:45
 */
@Data
public class RentBatteryQuery {

    /**
    * 换电柜id
    */
    @NotNull(message = "换电柜id不能为空!", groups = {UpdateGroup.class})
    private Integer electricityCabinetId;


}
