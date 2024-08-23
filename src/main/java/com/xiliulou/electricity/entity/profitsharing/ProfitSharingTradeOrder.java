package com.xiliulou.electricity.entity.profitsharing;

import java.math.BigDecimal;
import java.io.Serializable;

/**
 * 分账交易订单(TProfitSharingTradeOrder)实体类
 *
 * @author makejava
 * @since 2024-08-22 17:32:59
 */
public class ProfitSharingTradeOrder implements Serializable {
    private static final long serialVersionUID = 583343404538702651L;
    
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
     * 业务支付订单号
     */
    private String orderNo;
    /**
     * 第三方支付订单号
     */
    private String thirdOrderNo;
    /**
     * 订单类型：1-电池滞纳金，2-保险支付，3-换电套餐
     */
    private Integer orderType;
    /**
     * 支付金额,单位元
     */
    private BigDecimal amount;
    /**
     * 处理状态：0-初始化，1-待发起分账，2-分账发起成功，3-分账发起失败，4-已失效
     */
    private Integer processState;
    /**
     * 备注
     */
    private String remark;
    /**
     * 渠道：WECHAT-微信，ALIPAY-支付宝
     */
    private String channel;
    /**
     * 金额可退：0-是 1-否
     */
    private Integer rentRebate;
    /**
     * 支付时间
     */
    private Long payTime;
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

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getThirdOrderNo() {
        return thirdOrderNo;
    }

    public void setThirdOrderNo(String thirdOrderNo) {
        this.thirdOrderNo = thirdOrderNo;
    }

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getProcessState() {
        return processState;
    }

    public void setProcessState(Integer processState) {
        this.processState = processState;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Integer getRentRebate() {
        return rentRebate;
    }

    public void setRentRebate(Integer rentRebate) {
        this.rentRebate = rentRebate;
    }

    public Long getPayTime() {
        return payTime;
    }

    public void setPayTime(Long payTime) {
        this.payTime = payTime;
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

