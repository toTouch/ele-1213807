package com.xiliulou.electricity.vo.car;

import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 租车套餐展示层数据模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageVO implements Serializable {

    private static final long serialVersionUID = 8317006002657408755L;

    /**
     * 主键ID
     */
    private Long id;


    /**
     * 套餐名称
     */
    private String name;

    /**
     * 套餐类型
     * <pre>
     *     1-单车
     *     2-车电一体
     * </pre>
     * @see CarRentalPackageTypeEnum
     */
    private Integer type;

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
     * @see RentalUnitEnum
     */
    private Integer tenancyUnit;

    /**
     * 租金(元)
     */
    private BigDecimal rent;

    /**
     * 押金(元)
     */
    private BigDecimal deposit;

    /**
     * 车辆型号ID
     */
    private Integer carModelId;

    /**
     * 电池型号对应的电压伏数
     */
    private BigDecimal batteryV;

    /**
     * 适用类型
     * <pre>
     *     0-全部
     *     1-新租套餐
     *     2-续租套餐
     * </pre>
     * @see ApplicableTypeEnum
     */
    private Integer applicableType;

    /**
     * 租金可退
     * <pre>
     *     0-是
     *     1-否
     * </pre>
     * @see YesNoEnum
     */
    private Integer rentRebate;

    /**
     * 租金退还期限(天)
     */
    private Integer rentRebateTerm;

    /**
     * 免押
     * <pre>
     *     0-是
     *     1-否
     * </pre>
     * @see YesNoEnum
     */
    private Integer freeDeposit;

    /**
     * 租金单价
     */
    private BigDecimal rentUnitPrice;

    /**
     * 滞纳金(天)
     */
    private BigDecimal lateFee;

    /**
     * 套餐限制
     * <pre>
     *     0-不限制
     *     2-次数
     * </pre>
     * @see RenalPackageConfineEnum
     */
    private Integer confine;

    /**
     * 限制数量
     */
    private Long confineNum;

    /**
     * 优惠券赠送
     * <pre>
     *     0-是
     *     1-否
     * </pre>
     * @see YesNoEnum
     */
    private Integer giveCoupon;

    /**
     * 赠送的优惠券ID
     */
    private Long couponId;

    /**
     * 上下架状态
     * <pre>
     *     0-上架
     *     1-下架
     * </pre>
     * @see UpDownEnum
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Long createTime;

    // ++++++++++ 辅助业务数据 ++++++++++

    /**
     * 加盟商名称
     */
    private String franchiseeName;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 车辆型号名称
     */
    private String carModelName;

    /**
     * 赠送优惠券金额
     */
    private BigDecimal giveCouponAmount;

    /**
     * 电池型号编码集
     */
    private List<String> batteryModelTypes;

}
