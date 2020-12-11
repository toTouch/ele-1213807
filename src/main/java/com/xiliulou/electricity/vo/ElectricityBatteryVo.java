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

    //店铺名称
    private String shopName;
    //代理商名称
    private String agentName;
    //电池型号
    private String batteryModelName;
    //所在电柜名称
    private String cabinetName;
    /**
     * 所属用户
     */
    private String userName;

}
