package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * 换电柜表(TElectricityCabinet)实体类
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_cabinet")
public class ElectricityCabinet {
    /**
     * 换电柜Id
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /**
     * 电池编号
     */
    private String sn;
    /**
     * 换电柜名称
     */
    private String name;
    /**
     * 换电柜地址
     */
    private String address;
    /**
     * 地址经度
     */
    private Double longitude;
    /**
     * 地址纬度
     */
    private Double latitude;
    /**
     * 物联网productKey
     */
    private String productKey;
    /**
     * 物联网deviceName
     */
    private String deviceName;
    /**
     * 物联网deviceSecret
     */
    private String deviceSecret;
    /**
     * 可用状态(0--启用，1--禁用)
     */
    private Integer usableStatus;
    /**
     * 电源状态(0--通电，1--断电)
     */
    private Integer powerStatus;
    /**
     * 物联网连接状态（0--连网，1--断网）
     */
    private Integer onlineStatus;
    /**
     * 型号Id
     */
    private Integer modelId;
    /**
     * 版本
     */
    private String version;
    /**
     * 满电标准
     */
    private Double fullyCharged;
    /**
     * 联系电话
     */
    private String servicePhone;
    /**
     * 营业时间
     */
    private String businessTime;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;
    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;

    //租户id
    private Integer tenantId;

    //门店id
    private Integer storeId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    //可用
    public static final Integer ELECTRICITY_CABINET_USABLE_STATUS = 0;
    //禁用
    public static final Integer ELECTRICITY_CABINET_UN_USABLE_STATUS = 1;
    //通电
    public static final Integer ELECTRICITY_CABINET_POWER_STATUS = 0;
    //断电
    public static final Integer ELECTRICITY_CABINET_NO_POWER_STATUS = 1;
    //连网
    public static final Integer ELECTRICITY_CABINET_ONLINE_STATUS = 0;
    //断网
    public static final Integer ELECTRICITY_CABINET_OFFLINE_STATUS = 1;

}
