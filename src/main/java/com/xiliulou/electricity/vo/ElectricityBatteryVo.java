package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.entity.ElectricityBattery;
import lombok.Data;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-11-27 10:43
 **/
@Data
public class ElectricityBatteryVo extends ElectricityBattery {

    /**
     * 所属用户
     */
    private String userName;

}
