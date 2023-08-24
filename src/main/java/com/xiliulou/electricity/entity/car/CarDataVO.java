package com.xiliulou.electricity.entity.car;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import lombok.Data;

/**
 * 车辆数据模型
 */
@Data
public class CarDataVO {
    /**
     * 车辆Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 车辆sn
     */
    private String sn;

    /**
     * 车辆型号
     */
    private String model;

    /**
     * 地址经度
     */
    private Double longitude;
    /**
     * 地址纬度
     */
    private Double latitude;

    /**
     * 车辆状态 0--空闲 1--租借
     */
    private Integer status;

    private Long uid;

    private Long userInfoId;

    /**
     * 车辆绑定的用户名字
     */
    private String userName;

    /**
     * 门店Id
     */
    private Long storeId;
    private String storeName;

    private Long franchiseeId;
    private String franchiseeName;


    /**
     * 车辆型号Id
     */
    private Integer modelId;

    private String phone;

    /**
     * 绑定电池Sn码
     */
    private String batterySn;

    /**
     * 车辆是否锁定
     */
    private Integer lockType;

    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;

    private Integer tenantId;

    private Integer delFlag;

    /**
     * 套餐类型
     * <pre>
     *     1-单车
     *     2-车电一体
     * </pre>
     * @see RentalPackageTypeEnum
     */
    private Integer rentalPackageType;

    /**
     * 套餐名称
     */
    private String rentalPackageName;

    /**
     * 套餐ID
     */
    private Long rentalPackageId;

    /**
     * 总计到期时间
     */
    private Long dueTimeTotal;

}
