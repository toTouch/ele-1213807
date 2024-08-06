package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @author maxiaodong
 * @date 2024/2/19 21:19
 * @desc
 */
@Data
public class MerchantJoinRecordVO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 商户ID
     */
    private Long merchantId;
    
    /**
     * 渠道员uid
     */
    private Long channelEmployeeUid;
    
    /**
     * 场地ID
     */
    private Long placeId;
    
    /**
     * 邀请人uid
     */
    private Long inviterUid;
    
    /**
     * 用户名称
     */
    private String userName;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 商户名称
     */
    private String merchantName;
    
    /**
     * 邀请时间
     */
    private Long startTime;
    
    /**
     * 参与人uid
     */
    private Long joinUid;
    
    /**
     * 某个商户下的用户的数量
     */
    private Integer merchantUserNum;
    
    private Long franchiseeId;
    
    private String franchiseeName;
    
    /**
     * 邀请成功时间
     */
    private Long successTime;
}
