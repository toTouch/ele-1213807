package com.xiliulou.electricity.vo.asset;

import lombok.Data;

@Data
public class ElectricityCabinetModelVO {
    /**
     * 型号Id
     */
    private Integer id;
    
    /**
     * 厂家名称
     */
    private String manufacturerName ;
    
    /**
     * 型号名称
     */
    private String name;
    
    /**
     * 型号名称
     */
    private String manufacturerNameAndModelName;
}
