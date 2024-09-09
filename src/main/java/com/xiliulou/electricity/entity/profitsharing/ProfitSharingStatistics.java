package com.xiliulou.electricity.entity.profitsharing;

import java.math.BigDecimal;
import java.io.Serializable;

/**
 * 分账统计(TProfitSharingStatistics)实体类
 *
 * @author makejava
 * @since 2024-08-22 17:31:12
 */
public class ProfitSharingStatistics implements Serializable {
    private static final long serialVersionUID = 717750811454324078L;
    
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
     * 统计类型：1-按月统计
     */
    private Integer statisticsType;
    /**
     * 统计时间：按月统计为 yyyy-MM
     */
    private String statisticsTime;
    /**
     * 分账累计金额,单位元
     */
    private BigDecimal totalAmount;
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

    public Integer getStatisticsType() {
        return statisticsType;
    }

    public void setStatisticsType(Integer statisticsType) {
        this.statisticsType = statisticsType;
    }

    public String getStatisticsTime() {
        return statisticsTime;
    }

    public void setStatisticsTime(String statisticsTime) {
        this.statisticsTime = statisticsTime;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
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

