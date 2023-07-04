package com.xiliulou.electricity.query.car;

import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 租车套餐查询类
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
    private Integer limitNum = 10;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 加盟商ID
     */
    private Integer franchiseeId;

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
     * 上下架状态
     * <pre>
     *     1-上架
     *     2-下架
     * </pre>
     * @see UpDownEnum
     */
    private Integer status;

}
