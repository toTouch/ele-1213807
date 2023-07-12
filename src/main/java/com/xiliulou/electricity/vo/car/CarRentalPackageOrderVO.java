package com.xiliulou.electricity.vo.car;

import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 租车套餐购买订单展示层数据模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageOrderVO implements Serializable {

    private static final long serialVersionUID = -1774728302026416327L;

    /**
     * 订单编码
     */
    private String orderNo;


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
     * 套餐限制
     * <pre>
     *     1-不限制
     *     2-次数
     * </pre>
     * @see RenalPackageConfineEnum
     */
    private Integer confine;

    /**
     * 限制数量
     */
    private Integer confineNum;

    /**
     * 租期
     */
    private Integer tenancy;

    /**
     * 租期单位
     * <pre>
     *     1-天
     *     2-分钟
     * </pre>
     * @see TimeUnitEnum
     */
    private Integer tenancyUnit;

    /**
     * 租金单价，单位同租期单位
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
     * 车辆型号ID
     */
    private Integer carModelId;

    /**
     * 电池型号ID集，用英文逗号分割
     */
    private String batteryModelIds;

    /**
     * 电池型号对应的电压伏数
     */
    private BigDecimal batteryV;

    /**
     * 适用类型
     * <pre>
     *     1-全部
     *     2-新租套餐
     *     3-续租套餐
     * </pre>
     * @see ApplicableTypeEnum
     */
    private Integer applicableType;

    /**
     * 租金可退
     * <pre>
     *     1-是
     *     2-否
     * </pre>
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
     *     4-赠送
     * </pre>
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

    // ++++++++++ 辅助业务数据 ++++++++++

    /**
     * 加盟商名称
     */
    private String franchiseeName;

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

}
