package com.xiliulou.electricity.vo;

import java.math.BigDecimal;

/**
 * 退款订单表(TEleRefundOrder)实体类
 *
 * @author makejava
 * @since 2021-02-22 10:17:06
 */
public class EleRefundOrderVO {
    
    /**
     * 退款Id
     */
    private Long id;
    
    /**
     * 退款单号
     */
    private String refundOrderNo;
    
    /**
     * 支付单号
     */
    private String orderId;
    
    /**
     * 支付金额,单位元
     */
    private BigDecimal payAmount;
    
    /**
     * 退款金额,单位元
     */
    private BigDecimal refundAmount;
    
    /**
     * 退款状态:0--订单生成,1-退款中,2-退款成功,-1-退款失败
     */
    private Integer status;
    
    /**
     * 错误原因
     */
    private String errMsg;
    
    /**
     * 是否删除（0-正常，1-删除）
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
    
    /**
     * 用户Id
     */
    private Long uid;
    
    /**
     * 用户名
     */
    private String name;
    
    /**
     * 手机号
     */
    private String phone;
    
    private Integer payType;
    
    /**
     * 订单类型： 0-普通换电订单，1-企业渠道换电订单
     * @see PackageOrderTypeEnum
     */
    private Integer orderType;
    
    //private Integer refundOrderType;
    private Boolean isFreeDepositAliPay;
    
    public Boolean getIsFreeDepositAliPay() {
        return this.isFreeDepositAliPay;
    }
    
    public void setIsFreeDepositAliPay(Boolean isFreeDepositAliPay) {
        this.isFreeDepositAliPay = isFreeDepositAliPay;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getRefundOrderNo() {
        return refundOrderNo;
    }
    
    public void setRefundOrderNo(String refundOrderNo) {
        this.refundOrderNo = refundOrderNo;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public BigDecimal getPayAmount() {
        return payAmount;
    }
    
    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }
    
    public BigDecimal getRefundAmount() {
        return refundAmount;
    }
    
    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getErrMsg() {
        return errMsg;
    }
    
    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
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
    
    public Long getUid() {
        return uid;
    }
    
    public void setUid(Long uid) {
        this.uid = uid;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public Integer getPayType() {
        return payType;
    }
    
    public void setPayType(Integer payType) {
        this.payType = payType;
    }
    
    public Integer getOrderType() {
        return orderType;
    }
    
    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }
    
    //    public Integer getRefundOrderType() {
    //        return refundOrderType;
    //    }
    //
    //    public void setRefundOrderType(Integer refundOrderType) {
    //        this.refundOrderType = refundOrderType;
    //    }
    
}
