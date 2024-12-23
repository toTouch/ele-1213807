package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 订单表(TElectricityCabinetOrder)实体类
 *
 * @author makejava
 * @since 2020-11-26 16:00:45
 */
@Data
public class OrderQueryV2 {

    /**
     * 换电柜id
     */
    @NotNull(message = "换电柜id不能为空!")
    private Integer eid;
    /**
     * 下单来源 1--微信公众号 2--小程序
     */
    private Integer source;
    
    /**
     * 旧电池检测失败，灵活续费发生套餐转换，灵活续费为换电时，会拦截不分配电池，传1-开始换电
     */
    private Integer flexibleRenewalType;

    //微信公众号来源
    public static final Integer SOURCE_WX_MP = 1;
    //微信小程序来源
    public static final Integer SOURCE_WX_RPO = 2;


}
