package com.xiliulou.electricity.model.car.query;

import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 租车套餐，DB层查询模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageQryModel implements Serializable {

    private static final long serialVersionUID = 8570070520462619482L;

    /**
     * 偏移量
     */
    private Integer offset = 0;

    /**
     * 取值数量
     */
    private Integer size = 10;

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
     * 套餐名称
     */
    private String name;

    /**
     * 套餐类型
     * <pre>
     *     1-单车
     *     2-车电一体
     * </pre>
     * @see RentalPackageTypeEnum
     */
    private Integer type;


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
     * 租期单位
     * <pre>
     *     1-天
     *     0-分钟
     * </pre>
     * @see RentalUnitEnum
     */
    private Integer tenancyUnit;

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
     * 押金
     */
    private BigDecimal deposit;

    /**
     * 套餐限制
     * <pre>
     *     0-不限制
     *     1-次数
     * </pre>
     * @see RenalPackageConfineEnum
     */
    private Integer confine;

    /**
     * 车辆型号ID
     */
    private Integer carModelId;

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
     * 优惠券赠送
     * <pre>
     *     0-是
     *     1-否
     * </pre>
     * @see YesNoEnum
     */
    private Integer giveCoupon;

    /**
     * 主键ID集
     */
    private List<Long> idList;

    /**
     * 适用类型
     * <pre>
     *     0-全部
     *     1-新租套餐
     *     2-续租套餐
     * </pre>
     * @see ApplicableTypeEnum
     */
    private List<Integer> applicableTypeList;

    /**
     * 加盟商ID集
     */
    private List<Integer> franchiseeIdList;

    /**
     * 门店ID集
     */
    private List<Integer> storeIdList;

    /**
     * 免押
     * <pre>
     *     0-是
     *     1-否
     * </pre>
     * @see YesNoEnum
     */
    private Integer freeDeposit;

}
