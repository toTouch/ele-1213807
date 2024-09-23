package com.xiliulou.electricity.vo.profitsharing;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 分账接收方配置明细
 *
 * @author caobotao.cbt
 * @date 2024/8/26 14:07
 */
@Data
public class ProfitSharingReceiverConfigVO implements Serializable {
    
    private static final long serialVersionUID = 860446181051715540L;
    
    private Long id;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    
    
    /**
     * 微信商户号
     */
    private String wechatMerchantId;
    
    
    /**
     * 配置类型
     */
    private Integer configType;
    
    /**
     * 加盟商名称
     */
    private String franchiseeName;
    
    /**
     * 分账方配置表id
     */
    private Long profitSharingConfigId;
    
    /**
     * 分账接收方账户，接收方类型为商户则为商户id，接收方类型为个人则为openId，
     */
    private String account;
    
    /**
     * 接收方类型：1-商户，2-个人
     */
    private Integer receiverType;
    
    /**
     * 接收方账户名
     */
    private String receiverName;
    
    /**
     * 与分账方的关系类型
     */
    private String relationType;
    
    /**
     * 接收方状态：0-启用 1-禁用
     */
    private Integer receiverStatus;
    
    /**
     * 比例
     */
    private BigDecimal scale;
    
    /**
     * 备注
     */
    private String remark;
    
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    
}

