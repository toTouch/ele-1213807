package com.xiliulou.electricity.entity.profitsharing;

import java.math.BigDecimal;
import java.io.Serializable;

/**
 * 分账方配置表(TProfitSharingConfig)实体类
 *
 * @author makejava
 * @since 2024-08-22 17:14:08
 */
public class ProfitSharingConfig implements Serializable {
    private static final long serialVersionUID = 514513200105352426L;
    
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
     * 支付配置表id
     */
    private Integer payParamId;
    /**
     * 配置状态：0-启用 1-禁用
     */
    private Integer configStatus;
    /**
     * 订单类型：1-换电-套餐购买 ，2-换电-保险购买，4-换电-滞纳金缴纳（如同时选择多个类型，则之为类型之和）
     */
    private Integer orderType;
    /**
     * 每月最大分账上限
     */
    private BigDecimal amountLimit;
    /**
     * 分账类型：1-按订单比例
     */
    private Integer profitSharingType;
    /**
     * 允许比例上限
     */
    private BigDecimal scaleLimit;
    /**
     * 周期类型：1:D+1
     */
    private Integer cycleType;
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

    public Integer getPayParamId() {
        return payParamId;
    }

    public void setPayParamId(Integer payParamId) {
        this.payParamId = payParamId;
    }

    public Integer getConfigStatus() {
        return configStatus;
    }

    public void setConfigStatus(Integer configStatus) {
        this.configStatus = configStatus;
    }

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }

    public BigDecimal getAmountLimit() {
        return amountLimit;
    }

    public void setAmountLimit(BigDecimal amountLimit) {
        this.amountLimit = amountLimit;
    }

    public Integer getProfitSharingType() {
        return profitSharingType;
    }

    public void setProfitSharingType(Integer profitSharingType) {
        this.profitSharingType = profitSharingType;
    }

    public BigDecimal getScaleLimit() {
        return scaleLimit;
    }

    public void setScaleLimit(BigDecimal scaleLimit) {
        this.scaleLimit = scaleLimit;
    }

    public Integer getCycleType() {
        return cycleType;
    }

    public void setCycleType(Integer cycleType) {
        this.cycleType = cycleType;
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

