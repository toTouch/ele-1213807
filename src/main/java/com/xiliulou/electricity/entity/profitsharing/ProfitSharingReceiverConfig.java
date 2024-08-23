package com.xiliulou.electricity.entity.profitsharing;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 分账接收方配置表(TProfitSharingReceiverConfig)实体类
 *
 * @author makejava
 * @since 2024-08-22 17:27:48
 */
public class ProfitSharingReceiverConfig implements Serializable {
    private static final long serialVersionUID = 860446181051715540L;
    
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
     * 接收方状态：0-启用 1-禁用
     */
    private Integer receiverStatus;
    /**
     * 比例
     */
    private BigDecimal scale;
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

    public Long getProfitSharingConfigId() {
        return profitSharingConfigId;
    }

    public void setProfitSharingConfigId(Long profitSharingConfigId) {
        this.profitSharingConfigId = profitSharingConfigId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Integer getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(Integer receiverType) {
        this.receiverType = receiverType;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public Integer getReceiverStatus() {
        return receiverStatus;
    }

    public void setReceiverStatus(Integer receiverStatus) {
        this.receiverStatus = receiverStatus;
    }

    public BigDecimal getScale() {
        return scale;
    }

    public void setScale(BigDecimal scale) {
        this.scale = scale;
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

