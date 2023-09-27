package com.xiliulou.electricity.vo.car;

import com.xiliulou.electricity.enums.PayTypeEnum;
import com.xiliulou.electricity.enums.RefundStateEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 租车套餐押金退款展现层数据模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageDepositRefundVo implements Serializable {

    private static final long serialVersionUID = 1431787925860312325L;

    /**
     * 订单编码
     */
    private String orderNo;

    /**
     * 押金缴纳订单编号
     */
    private String depositPayOrderNo;

    /**
     * 套餐类型
     * <pre>
     *     1-单车
     *     2-车电一体
     * </pre>
     *
     * @see RentalPackageTypeEnum
     */
    private Integer rentalPackageType;

    /**
     * 申请金额
     */
    private BigDecimal applyAmount;

    /**
     * 实际退款金额
     */
    private BigDecimal realAmount;

    /**
     * 交易方式
     * <pre>
     *     1-线上
     *     2-线下
     *     3-免押
     * </pre>
     *
     * @see PayTypeEnum
     */
    private Integer payType;

    /**
     * 退款订单状态
     * <pre>
     *     1-待审核
     *     2-审核通过
     *     3-审核拒绝
     *     4-退款中
     *     5-退款成功
     *     6-退款失败
     * </pre>
     *
     * @see RefundStateEnum
     */
    private Integer refundState;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 备注
     */
    private String remark;

    // ++++++++++ 辅助业务数据 ++++++++++

    /**
     * 用户UID
     */
    private Long uid;

    /**
     * 用户真实姓名
     */
    private String userRelName;

    /**
     * 用户手机号
     */
    private String userPhone;

}
