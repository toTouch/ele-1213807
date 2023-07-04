package com.xiliulou.electricity.vo.car;

import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 租车套餐展示层数据模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageVO implements Serializable {

    private static final long serialVersionUID = 8317006002657408755L;


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
     *     2-分钟
     * </pre>
     * @see TimeUnitEnum
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
     * 电池型号ID集，用英文逗号分割
     */
    private String batteryModelIds;

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
     * 免押
     * <pre>
     *     1-否
     *     2-芝麻信用
     * </pre>
     * @see DepositExemptionEnum
     */
    private Integer depositExemption;

    /**
     * 押金返还审批
     * <pre>
     *     1-是
     *     2-否
     * </pre>
     * @see YesNoEnum
     */
    private Integer depositRebateApprove;

    /**
     * 租金单价，单位同租期单位
     */
    private BigDecimal rentUnitPrice;

    /**
     * 滞纳金(天)
     */
    private BigDecimal lateFee;

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
     * 优惠券赠送
     * <pre>
     *     1-是
     *     2-否
     * </pre>
     * @see YesNoEnum
     */
    private Integer giveCoupon;

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 上下架状态
     * <pre>
     *     1-上架
     *     2-下架
     * </pre>
     * @see UpDownEnum
     */
    private Integer status;

    /**
     * C端展示
     * <pre>
     *     1-是
     *     2-否
     * </pre>
     * @see YesNoEnum
     */
    private Integer showFlag;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Integer createTime;

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

}
