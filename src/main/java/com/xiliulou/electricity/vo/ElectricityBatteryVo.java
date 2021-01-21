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

    //所在电柜名称
    private String cabinetName;
    /**
     * 所属用户
     */
    private String userName;
    //电池型号容量
    private Integer capacity;
    //电压
    private Integer voltage;

}
