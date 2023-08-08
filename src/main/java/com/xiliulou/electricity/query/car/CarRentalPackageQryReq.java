package com.xiliulou.electricity.query.car;

import com.xiliulou.electricity.enums.ApplicableTypeEnum;
import com.xiliulou.electricity.enums.RentalUnitEnum;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 租车套餐表，查询模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageQryReq implements Serializable {

    private static final long serialVersionUID = -626924925797065186L;

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
     * 用户ID
     */
    private Long uid;

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
     * 车辆型号ID
     */
    private Integer carModelId;

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
     * 套餐类型
     * <pre>
     *     1-单车
     *     2-车电一体
     * </pre>
     * @see RentalPackageTypeEnum
     */
    private Integer rentalPackageType;

}
