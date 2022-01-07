package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author: Miss.Li
 * @Date: 2021/8/30 09:33
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_withdraw_record")
public class WithdrawRecord {
    /**
    * 主键Id
    */
    @TableId(value = "id",type = IdType.AUTO)
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
    * 手续费,单位元
    */
    private Double handlingFee;
    /**
     * 提现到账金额,单位元
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


    //银行卡号绑定人
    private String trueName;

    //银行编号
    private String bankCode;

    private Integer tenantId;


    //状态
    //审核中
	public static final Integer CHECKING = 1;
    //审核拒绝
	public static final Integer CHECK_REFUSE = 2;
    //审核通过
    public static final Integer CHECK_PASS = 3;
    //提现中
	public static final Integer WITHDRAWING = 4;
    //提现成功
    public static final Integer WITHDRAWING_SUCCESS = 5;
    //提现失败
    public static final Integer WITHDRAWING_FAIL = 6;


    //线上
    public static final Integer TYPE_ONLINE = 1;
    //线下
    public static final Integer TYPE_UN_ONLINE = 2;

}
