package com.xiliulou.electricity.vo;
import lombok.Data;

import java.util.Set;

/**
 * 换电柜表(TElectricityCabinet)实体类
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@Data
public class ElectricityCabinetVO {
    /**
    * 换电柜Id
    */
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
    * 换电柜地区Id
    */
    private Integer areaId;
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
    /**
     * 型号名称
     */
    private String modelName;
    /**
     * 电池总数
     */
    private Integer electricityBatteryTotal;
    /**
     * 满电仓
     */
    private Integer fullyElectricityBattery;
    /**
     * 空仓
     */
    private Integer noElectricityBattery;
    /**
     * 电池规格
     */
    private Set<String> electricityBatteryFormat;

}