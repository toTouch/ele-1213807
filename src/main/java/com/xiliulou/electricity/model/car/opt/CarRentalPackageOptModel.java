package com.xiliulou.electricity.model.car.opt;

import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 租车套餐，操作模型<br />
 * 转换DB
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
     *     0-分钟
     * </pre>
     * @see RentalUnitEnum
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
    @NotNull(message = "[押金]不能为空", groups = {CreateGroup.class})
    private BigDecimal deposit;

    /**
     * 车辆型号ID
     */
    @NotNull(message = "[车辆型号]不能为空", groups = {CreateGroup.class})
    private Integer carModelId;

    /**
     * 电池型号编码集
     */
    private List<String> batteryModelTypes;

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
    @NotNull(message = "[租赁类型]不能为空")
    private Integer applicableType;

    /**
     * 租金可退
     * <pre>
     *     0-是
     *     1-否
     * </pre>
     * @see YesNoEnum
     */
    @NotNull(message = "[租金可退]不能为空")
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
    @NotNull(message = "[免押]不能为空")
    private Integer freeDeposit;

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
     *     0-不限制
     *     1-次数
     * </pre>
     * @see RenalPackageConfineEnum
     */
    @NotNull(message = "[套餐限制]不能为空")
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
    @NotNull(message = "[优惠券赠送]不能为空")
    private Integer giveCoupon;

    /**
     * 优惠券ID
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
}
