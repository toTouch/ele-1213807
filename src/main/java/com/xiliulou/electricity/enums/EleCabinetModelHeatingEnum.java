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
 *    <h4>排期内快速实现</h4>
 *    <a herf="https://benyun.feishu.cn/wiki/GrNjwBNZkipB5wkiws2cmsEDnVU#KZJedQiWgoiJpQxCPM0cErGqndR">12.2 电柜厂家型号（3条优化点）</a>
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/3/12
**/
@Getter
@AllArgsConstructor
public enum EleCabinetModelHeatingEnum implements BasicEnum<Integer,String> {
    HEATING_SUPPORT(1,"柜机加热支持"),
    HEATING_NOT_SUPPORT(1,"柜机加热不支持");
    
    private final Integer code;
    
    private final String desc;
}
