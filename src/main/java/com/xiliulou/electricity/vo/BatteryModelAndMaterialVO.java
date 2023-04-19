package com.xiliulou.electricity.vo;

import lombok.Data;

import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-19-10:51
 */
@Data
public class BatteryModelAndMaterialVO {
    private List<BatteryMaterialVO> batteryMaterials;
    private List<BatteryModelVO> batteryModels;
}
