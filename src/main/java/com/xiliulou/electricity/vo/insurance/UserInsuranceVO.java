package com.xiliulou.electricity.vo.insurance;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 用户保险信息
 *
 * @author xiaohui.song
 **/
@Data
public class UserInsuranceVO implements Serializable {
    
    private static final long serialVersionUID = -4443513794335742581L;
    
    /**
     * 保险名称
     */
    private String insuranceName;
    
    /**
     * 过期时间
     */
    private Long insuranceExpireTime;
    
    /**
     * 保费
     */
    private BigDecimal premium;
    
    
    private Integer id;
    
    private Long uid;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 保额
     */
    private BigDecimal forehead;
    
    /**
     * 保险Id
     */
    private Integer insuranceId;
    
    /**
     * 保险订单编号
     */
    private String orderId;
    
    /**
     * 是否出险 0--未出险 1--已出险
     */
    private Integer isUse;
    
    private Long createTime;
    
    private String cityName;
    
    private Integer cid;
    
    /**
     * 保险购买时间
     */
    private Long payInsuranceTime;
    
    /**
     * 保险类型 0-电 1-车 2-车电
     */
    private Integer type;
    
    
}
