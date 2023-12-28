package com.xiliulou.electricity.vo;


import lombok.Data;

/**
 * @author maxiaodong
 * @date 2023/12/28 13:38
 * @desc
 */
@Data
public class TenantNoteRechargeVo {
    /**
     * 主键Id
     */
    private Long id;
    
    /**
     * 租户Id
     */
    private Integer tenantId;
    
    /**
     * 租户短信表id
     */
    private Long tenantNoteId;
    
    /**
     * 操作人Id
     */
    private Long uid;
    
    /**
     * 操作人名称
     */
    private String uName;
    
    /**
     * 短信充值次数
     */
    private Integer rechargeNum;
    
    /**
     * 充值时间
     */
    private Long rechargeTime;
    
    /**
     * 创建时间
     */
    private Long createTime;
}
