package com.xiliulou.electricity.vo.car;

import com.xiliulou.electricity.enums.ApplicableTypeEnum;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.enums.PayTypeEnum;
import com.xiliulou.electricity.enums.RenalPackageConfineEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.enums.RentalUnitEnum;
import com.xiliulou.electricity.enums.UseStateEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 租车套餐购买订单展示层数据模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageOrderVo implements Serializable {
    
    private static final long serialVersionUID = -1774728302026416327L;
    
    /**
     * 订单编码
     */
    private String orderNo;
    
    /**
     * 套餐ID
     */
    private Long rentalPackageId;
    
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
     * 套餐限制
     * <pre>
     *     0-不限制
     *     1-次数
     * </pre>
     *
     * @see RenalPackageConfineEnum
     */
    private Integer confine;
    
    /**
     * 限制数量
     */
    private Long confineNum;
    
    /**
     * 租期
     */
    private Integer tenancy;
    
    /**
     * 租期单位
     * <pre>
     *     1-天
     *     0-分钟
     * </pre>
     *
     * @see RentalUnitEnum
     */
    private Integer tenancyUnit;
    
    /**
     * 租金单价
     */
    private BigDecimal rentUnitPrice;
    
    /**
     * 租金(原价)
     */
    private BigDecimal rent;
    
    /**
     * 租金(支付价格)
     */
    private BigDecimal rentPayment;
    
    /**
     * 适用类型
     * <pre>
     *     0-全部
     *     1-新租套餐
     *     2-续租套餐
     * </pre>
     *
     * @see ApplicableTypeEnum
     */
    private Integer applicableType;
    
    /**
     * 租金可退
     * <pre>
     *     0-是
     *     1-否
     * </pre>
     *
     * @see YesNoEnum
     */
    private Integer rentRebate;
    
    /**
     * 租金退还期限(天)
     */
    private Integer rentRebateTerm;
    
    /**
     * 租金退还截止时间
     */
    private Long rentRebateEndTime;
    
    /**
     * 押金缴纳订单编号
     */
    private String depositPayOrderNo;
    
    /**
     * 滞纳金(天)
     */
    private BigDecimal lateFee;
    
    /**
     * 交易方式
     * <pre>
     *     1-线上
     *     2-线下
     * </pre>
     *
     * @see PayTypeEnum
     */
    private Integer payType;
    
    /**
     * 赠送的优惠券ID
     */
    private Long couponId;
    
    /**
     * 支付状态
     * <pre>
     *     1-未支付
     *     2-支付成功
     *     3-支付失败
     *     4-取消支付
     * </pre>
     *
     * @see PayStateEnum
     */
    private Integer payState;
    
    /**
     * 使用状态
     * <pre>
     *     1-未使用
     *     2-使用中
     *     3-已失效
     *     4-已退租
     * </pre>
     *
     * @see UseStateEnum
     */
    private Integer useState;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间，时间戳
     */
    private Long createTime;
    
    /**
     * 套餐押金
     */
    private BigDecimal rentalPackageDeposit;
    
    // ++++++++++ 辅助业务数据 ++++++++++
    
    /**
     * 加盟商名称
     */
    private String franchiseeName;
    
    /**
     * 用户ID
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
    
    /**
     * 租车套餐名称
     */
    private String carRentalPackageName;
    
    /**
     * 车辆型号名称
     */
    private String carModelName;
    
    /**
     * 赠送的优惠券名称
     */
    private String couponName;
    
    /**
     * 电池型号对应的电压伏数
     */
    private String batteryVoltage;
    
    /**
     * 押金金额(元)
     */
    private BigDecimal deposit;
    
    /**
     * 退租状态
     */
    private Integer rentRefundStatus;
    
    /**
     * 套餐冻结状态
     */
    private Integer freezeStatus;
    
    /**
     * 退租拒绝原因
     */
    private String rejectReason;
    
}
