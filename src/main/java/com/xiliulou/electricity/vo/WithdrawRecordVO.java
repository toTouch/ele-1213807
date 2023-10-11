package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: Miss.Li
 * @Date: 2021/8/30 09:33
 * @Description:
 */
@Data
public class WithdrawRecordVO {

    /**
     * 主键Id
     */
    private Long id;

    /**
     * 用户Id
     */
    private Long uid;

    /**
     * 提现订单号
     */
    private String orderId;

    /**
     * 银行名称
     */
    private String bankName;

    /**
     * 银行卡号
     */
    private String bankNumber;

    /**
     * 请求提现,单位元
     */
    private Double requestAmount;

    /**
     * 平台服务费,单位元
     */
    private Double platformFee;

    /**
     * 手续费,单位元
     */
    private Double handlingFee;

    /**
     * 提现实际到账金额,单位元
     */
    private Double amount;

    /**
     * 审核时间
     */
    private Long checkTime;

    /**
     * 到账时间
     */
    private Long arriveTime;

    /**
     * 状态 1--审核中 2--审核拒绝  3--审核通过  4--提现中  5--提现成功  6--提现失败
     */
    private Integer status;

    /**
     * 类型 1--线上 2--线下
     */
    private Integer type;

    private Long createTime;

    private Long updateTime;

    /**
     * 提现错误信息
     */
    private String msg;

    /**
     * 银行卡号绑定人
     */
    private String trueName;

    /**
     * 银行编号
     */
    private String bankCode;

    /**
     * phone
     */
    private String phone;

    /**
     * 身份证号
     */
    private String IdNumber;


    /**
     * 平台税率
     */
    private BigDecimal serviceTaxRate;
}
