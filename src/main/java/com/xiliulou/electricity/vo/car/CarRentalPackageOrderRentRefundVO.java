package com.xiliulou.electricity.vo.car;

import com.xiliulou.electricity.enums.RefundStateEnum;
import com.xiliulou.electricity.enums.RentalUnitEnum;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 租车套餐订单退租展现层数据模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageOrderRentRefundVO implements Serializable {

    private static final long serialVersionUID = 2271729616539732402L;

    /**
     * 订单编码
     */
    private String orderNo;

    /**
     * 购买订单编号
     */
    private String rentalPackageOrderNo;

    /**
     * 套餐类型
     * <pre>
     *     1-单车
     *     2-车电一体
     * </pre>
     * @see CarRentalPackageTypeEnum
     */
    private Integer rentalPackageType;

    /**
     * 余量
     */
    private Long residue;

    /**
     * 余量单位
     * <pre>
     *     -1-次数
     *     1-天
     *     0-分钟
     * </pre>
     * @see RentalUnitEnum
     */
    private Integer residueUnit;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

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
     * @see RefundStateEnum
     */
    private Integer refundState;

    /**
     * 租金单价
     */
    private BigDecimal rentUnitPrice;

    /**
     * 租金(支付价格)
     */
    private BigDecimal rentPayment;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;


    // ++++++++++ 辅助业务数据 ++++++++++

    /**
     * 用户真实姓名
     */
    private String userRelName;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 租车套餐名称
     */
    private String carRentalPackageName;

}
