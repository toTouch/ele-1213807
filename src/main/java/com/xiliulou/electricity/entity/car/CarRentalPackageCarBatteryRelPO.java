package com.xiliulou.electricity.entity.car;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.entity.basic.BasicCarPO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 租车套餐车辆电池关联表
 *
 * @author xiaohui.song
 **/
@Data
@TableName("t_car_rental_package_car_battery_rel")
public class CarRentalPackageCarBatteryRelPO extends BasicCarPO {

    /**
     * 套餐ID
     */
    private Long rentalPackageId;

    /**
     * 车辆型号ID
     */
    private Integer carModelId;

    /**
     * 电池型号编码
     */
    private String batteryModelType;

    /**
     * 电池型号对应的电压伏数
     */
    private String batteryVoltage;
}
