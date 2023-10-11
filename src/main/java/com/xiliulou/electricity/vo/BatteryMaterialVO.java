package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-11-14:13
 */
@Data
public class BatteryMaterialVO {

    /**
     * 类型
     */
    private String type;

    private String shortType;

    /**
     * 材料体系
     */
    private Integer kind;
}
