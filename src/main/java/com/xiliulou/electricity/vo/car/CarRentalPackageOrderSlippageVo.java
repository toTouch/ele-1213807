package com.xiliulou.electricity.vo.car;

import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.enums.SlippageTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 租车套餐订单逾期展现层数据模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageOrderSlippageVo implements Serializable {

    private static final long serialVersionUID = -7286569048630135594L;

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
     * 类型
     * <pre>
     *     1-过期
     *     2-冻结
     * </pre>
     *
     * @see SlippageTypeEnum
     */
    private Integer type;

    /**
     * 车辆型号ID
     */
    private Integer carModelId;

    /**
     * 车辆SN码
     */
    private String carSn;

    /**
     * 电池型号长编码
     */
    private String batteryModelType;

    /**
     * 电池SN码
     */
    private String batterySn;

    /**
     * 滞纳金(元/天)
     */
    private BigDecimal lateFee;

    /**
     * 滞纳金开始时间
     */
    private Long lateFeeStartTime;

    /**
     * 滞纳金结束时间
     */
    private Long lateFeeEndTime;

    /**
     * 滞纳金应缴纳金额
     */
    private BigDecimal lateFeePayable;

    /**
     * 滞纳金缴纳金额
     */
    private BigDecimal lateFeePay;

    /**
     * 支付状态
     * <pre>
     *     1-未支付
     *     2-支付成功
     *     3-支付失败
     *     5-已清除
     * </pre>
     *
     * @see PayStateEnum
     */
    private Integer payState;

    /**
     * 创建时间，时间戳
     */
    private Long createTime;

    /**
     * 支付时间
     */
    private Long payTime;

    // ++++++++++ 辅助业务数据 ++++++++++

    /**
     * 加盟商名称
     */
    private String franchiseeName;

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

    /**
     * 套餐名称
     */
    private String rentalPackageName;

    /**
     * 门店名称
     */
    private String storeName;
    
    /**
     * @see com.xiliulou.core.base.enums.ChannelEnum
     */
    private String paymentChannel;
    
    /**
     * 套餐过期保护期，单位:小时，0-24表示套餐过期滞纳金起算时间 (冻结滞纳金为null)
     */
    private Integer expiredProtectionTime;
}
