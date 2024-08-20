package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-03-15-10:45
 */
@Data
public class FreeDepositOrderVO {
    
    private Long id;
    
    private Long uid;
    
    private String orderId;
    
    /**
     * 授权码
     */
    private String authNo;
    
    /**
     * 支付宝绑定的手机号
     */
    private String phone;
    
    /**
     * 身份征号
     */
    private String idCard;
    
    /**
     * 用户真实姓名
     */
    private String realName;
    
    /**
     * 免押金额
     */
    private Double transAmt;
    
    /**
     * 实际支付金额
     */
    private Double payTransAmt;
    
    /**
     * 支付状态
     */
    private Integer authStatus;
    
    /**
     * 授权免押的状态
     */
    private Integer payStatus;
    
    private Integer tenantId;
    
    /**
     * 免押的类型0--支付宝
     */
    private Integer type;
    
    /**
     * 押金类型 1：电池，2：租车
     */
    private Integer depositType;
    
    private Long createTime;
    
    private Long updateTime;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    private String franchiseeName;
}
