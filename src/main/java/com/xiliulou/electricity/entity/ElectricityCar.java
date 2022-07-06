package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 换电柜表(TElectricityCar)实体类
 *
 * @author makejava
 * @since 2022-06-06 11:00:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_car")
public class ElectricityCar {
    /**
     * 换电柜车辆绑定Id
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
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;

    private Integer tenantId;

    private Integer delFlag;

    public static final Integer CAR_NOT_RENT = 0;
    public static final Integer CAR_IS_RENT = 1;


    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;


}
