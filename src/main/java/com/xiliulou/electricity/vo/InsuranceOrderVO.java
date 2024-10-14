package com.xiliulou.electricity.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 换电柜电池表(InsuranceOrder)实体类
 *
 * @author makejava
 * @since 2022-11-04 14:44:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InsuranceOrderVO {
    
    private String orderId;
    
    private String franchiseeName;
    
    private String insuranceName;
    
    private Integer insuranceType;
    
    private Long uid;
    
    private String userName;
    
    private String idCard;
    
    private String phone;
    
    private Integer cid;
    
    private Long insuranceExpireTime;
    
    private BigDecimal insuranceAmount;
    
    private Long createTime;
    
    private Long updateTime;
    
    private Integer validDays;
    
    private String cityName;
    
    private Integer status;
    
    private BigDecimal forehead;
    
    private Integer isUse;
    
    private Integer payType;
    
    /**
     * 来源订单编码
     */
    private String sourceOrderNo;
    
    /**
     * <p>
     * Description: 保险ID
     * <a herf="https://benyun.feishu.cn/wiki/GrNjwBNZkipB5wkiws2cmsEDnVU#QZIhddTgBoCWAXxcwAjch0MGnIg">14.11 保险购买记录（3条优化项）</a>
     * </p>
     */
    private Long insuranceId;
    
    /**
     * <p>
     * Description: 汽车型号
     * <a herf="https://benyun.feishu.cn/wiki/GrNjwBNZkipB5wkiws2cmsEDnVU#QZIhddTgBoCWAXxcwAjch0MGnIg">14.11 保险购买记录（3条优化项）</a>
     * </p>
     */
    private String carModel;
    
    /**
     * <p>
     * Description: 电池型号
     * <a herf="https://benyun.feishu.cn/wiki/GrNjwBNZkipB5wkiws2cmsEDnVU#QZIhddTgBoCWAXxcwAjch0MGnIg">14.11 保险购买记录（3条优化项）</a>
     * </p>
     */
    private String batteryModel;
    
    /**
     * 电池型号
     */
    private String simpleBatteryType;
    
    
    /**
     * 支付渠道 WECHAT-微信支付,ALIPAY-支付宝
     */
    private String paymentChannel ;
}
