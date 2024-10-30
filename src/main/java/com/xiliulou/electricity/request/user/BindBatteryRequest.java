package com.xiliulou.electricity.request.user;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 订单表(TElectricityCabinetOrder)实体类
 *
 * @author maxiaodong
 * @since 2020-11-26 16:00:45
 */
@Data
public class BindBatteryRequest {
    /**
    * 换电柜id
    */
    @NotBlank(message = "电池编号不能为空!")
    private String batterySn;
}
