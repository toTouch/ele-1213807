package com.xiliulou.electricity.entity.profitsharing;

import java.math.BigDecimal;
import java.io.Serializable;

/**
 * 分账交易混合订单(TProfitSharingTradeMixedOrder)实体类
 *
 * @author makejava
 * @since 2024-08-27 19:19:18
 */
public class ProfitSharingTradeMixedOrder implements Serializable {
    
    private static final long serialVersionUID = -80537955302595275L;
    
    private Long id;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 第三方商户号
     */
    private String thirdMerchantId;
    
    /**
     * 第三方支付订单号
     */
    private String thirdOrderNo;
    
    /**
     * 处理状态：0-待处理，1-已完成
     */
    private Integer state;
    
    /**
     * 支付金额,单位元
     */
    private BigDecimal amount;
    
    /**
     * WECHAT-微信
     */
    private String channel;
    
    /**
     * 删除标识：0-未删除 1-已删除
     */
    private Integer delFlag;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getState() {
        return state;
    }
    
    public void setState(Integer state) {
        this.state = state;
    }
    
    public Integer getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }
    
    public Long getFranchiseeId() {
        return franchiseeId;
    }
    
    public void setFranchiseeId(Long franchiseeId) {
        this.franchiseeId = franchiseeId;
    }
    
    public String getThirdMerchantId() {
        return thirdMerchantId;
    }
    
    public void setThirdMerchantId(String thirdMerchantId) {
        this.thirdMerchantId = thirdMerchantId;
    }
    
    public String getThirdOrderNo() {
        return thirdOrderNo;
    }
    
    public void setThirdOrderNo(String thirdOrderNo) {
        this.thirdOrderNo = thirdOrderNo;
    }
    
    public String getChannel() {
        return channel;
    }
    
    public void setChannel(String channel) {
        this.channel = channel;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public Integer getDelFlag() {
        return delFlag;
    }
    
    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }
    
    public Long getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
    
    public Long getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
    
}

