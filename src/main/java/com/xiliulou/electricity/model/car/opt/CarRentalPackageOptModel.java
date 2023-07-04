package com.xiliulou.electricity.model.car.opt;

import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 租车套餐，DB层操作模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageOptModel implements Serializable {

    private static final long serialVersionUID = 4230345848040858112L;

    /**
     * 主键ID
     */
    @NotNull(message = "[主键ID]不能为空", groups = {UpdateGroup.class})
    private Long id;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 加盟商ID
     */
    private Integer franchiseeId;

    /**
     * 门店ID
     */
    private Integer storeId;

    /**
     * 创建人ID
     */
    private Long createUid;

    /**
     * 修改人ID
     */
    private Long updateUid;

    /**
     * 创建时间，时间戳
     */
    private Long createTime;

    /**
     * 修改时间，时间戳
     */
    private Long updateTime;

    /**
     * 删除标识
     * <pre>
     *     0-正常
     *     1-删除
     * </pre>
     * @see DelFlagEnum
     */
    private Integer delFlag = DelFlagEnum.OK.getCode();


    /**
     * 套餐名称
     */
    @NotNull(message = "[套餐名称]不能为空")
    private String name;

    /**
     * 套餐类型
     * <pre>
     *     1-单车
     *     2-车电一体
     * </pre>
     * @see CarRentalPackageTypeEnum
     */
    @NotNull(message = "[套餐类型]不能为空", groups = {CreateGroup.class})
    private Integer type;

    /**
     * 租期
     */
    @NotNull(message = "[租期]不能为空")
    private Integer tenancy;

    /**
     * 租期单位
     * <pre>
     *     1-天
     *     2-分钟
     * </pre>
     * @see TimeUnitEnum
     */
    @NotNull(message = "[租期单位]不能为空")
    private Integer tenancyUnit;

    /**
     * 租金
     */
    @NotNull(message = "[租金]不能为空")
    private BigDecimal rent;

    /**
     * 押金
     */
    @NotNull(message = "[押金]不能为空")
    private BigDecimal deposit;

    /**
     * 车辆型号ID
     */
    @NotNull(message = "[车辆型号]不能为空")
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
    @NotNull(message = "[租赁类型]不能为空")
    private Integer applicableType;

    /**
     * 租金可退
     * <pre>
     *     1-是
     *     2-否
     * </pre>
     * @see YesNoEnum
     */
    @NotNull(message = "[租金可退]不能为空")
    private Integer rentRebate;

    /**
     * 租金退还期限(天)
     */
    @NotNull(message = "[租金退还期限]不能为空")
    private Integer rentRebateTerm;

    /**
     * 免押
     * <pre>
     *     1-否
     *     2-芝麻信用
     * </pre>
     * @see DepositExemptionEnum
     */
    @NotNull(message = "[免押]不能为空")
    private Integer depositExemption;

    /**
     * 押金返还审批
     * <pre>
     *     1-是
     *     2-否
     * </pre>
     * @see YesNoEnum
     */
    @NotNull(message = "[押金返还审批]不能为空")
    private Integer depositRebateApprove;

    /**
     * 租金单价，单位同租期单位
     */
    @NotNull(message = "[租金单价]不能为空")
    private BigDecimal rentUnitPrice;

    /**
     * 滞纳金(天)
     */
    @NotNull(message = "[滞纳金]不能为空")
    private BigDecimal lateFee;

    /**
     * 套餐限制
     * <pre>
     *     1-不限制
     *     2-次数
     * </pre>
     * @see RenalPackageConfineEnum
     */
    @NotNull(message = "[套餐限制]不能为空")
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
    @NotNull(message = "[优惠券赠送]不能为空")
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
    @NotNull(message = "[是否展示]不能为空")
    private Integer showFlag;

    /**
     * 备注
     */
    private String remark;
}
