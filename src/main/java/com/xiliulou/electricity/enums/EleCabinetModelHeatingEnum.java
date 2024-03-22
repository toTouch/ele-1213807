package com.xiliulou.electricity.enums;


import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 *    Description: This enum is EleCabinetModelHeatingEnum!
 *    前端区分使用,标记柜机加热是否支持
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 * @since V1.0 2024/3/12
**/
@Getter
@AllArgsConstructor
public enum EleCabinetModelHeatingEnum implements BasicEnum<Integer,String> {
    HEATING_SUPPORT(1,"柜机加热支持"),
    HEATING_NOT_SUPPORT(0,"柜机加热不支持");
    
    private final Integer code;
    
    private final String desc;
}
