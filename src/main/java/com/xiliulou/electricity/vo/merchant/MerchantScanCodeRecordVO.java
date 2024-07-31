package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @author maxiaodong
 * @date 2024/2/19 21:19
 * @desc
 */
@Data
public class MerchantScanCodeRecordVO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 商户ID
     */
    private Long merchantId;
    
    
    /**
     * 商户名称
     */
    private String merchantName;
    
    /**
     * 参与人uid
     */
    private Long joinUid;
    
    /**
     * 用户名称
     */
    private String userName;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 参与状态
     */
    private Integer status;
    
    /**
     * 邀请时间
     */
    private Long startTime;
    
    /**
     * 过期时间
     */
    private Long expiredTime;
    
    /**
     * 套餐id
     */
    private Long cardId;
    
    /**
     * 套餐名称
     */
    private String cardName;
    
    /**
     * 订单id
     */
    private Long orderId;
    
    /**
     * 订单购买时间
     */
    private Long orderBuyTime;
    
    /**
     * 是否删除
     */
    private Integer delFlag;
    
    private Long franchiseeId;
    
    private String franchiseeName;
    
    private Long createTime;
    
    /**
     * 用户删除时间
     */
    private Long delTime;
}
