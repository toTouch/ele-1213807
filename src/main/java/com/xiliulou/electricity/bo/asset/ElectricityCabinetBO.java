package com.xiliulou.electricity.bo.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author HeYafeng
 * @description 柜机BO
 * @date 2023/11/27 14:02:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ElectricityCabinetBO {
    /**
     * 换电柜Id
     */
    private Integer id;
    /**
     * 换电柜名称
     */
    private String name;
    /**
     * 平台名称
     */
    private String configName;
    /**
     * 换电柜sn
     */
    private String sn;
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
     * 物联网连接状态（0--连网，1--断网）
     */
    private Integer onlineStatus;
    /**
     * 型号Id
     */
    private Integer modelId;
    
    /**
     * 设置厂家型号（厂家名称/型号）
     */
    private String manufacturerNameAndModelName;
    
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
     * 营业时间类型
     */
    private String businessTimeType;
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
    private Integer fullyBatteryNumber;
    /**
     * 空仓
     */
    private Integer noElectricityBattery;
    
    /**
     * 在仓
     */
    private Integer batteryInElectricity;
    
    /**
     * 可换电
     */
    private Integer exchangeBattery;
    
    
    private Double distance;
    
    /**
     * 营业开始时间
     */
    private Long beginTime;
    /**
     * 营业结束时间
     */
    private Long endTime;
    /**
     * 是否营业 0--营业 1--打烊
     */
    private Integer isBusiness;
    /**
     * 是否锁住 0--未锁住 1--锁住
     */
    private Integer isLock;
    
    //门店id
    private Integer storeId;
    private String storeName;
    
    /**
     * 满电标准
     */
    private Double batteryFullCondition;
    
    //租户id
    private Integer tenantId;
    
    //租户code
    private String tenantCode;
    
    /**
     * 服务开始时间
     */
    private Long serverBeginTime;
    /**
     * 服务结束时间
     */
    private Long serverEndTime;
    
    /**
     * 加盟商名字
     */
    private String franchiseeName;
    
    /**
     * 加盟商ID
     */
    private Long franchiseeId;
    
    /**
     * 柜机图片
     */
    private String pictureUrl;
    
    private Integer exchangeType;
    
    /**
     * 库房id
     */
    private Long warehouseId;
    
    /**
     * 库房名称
     */
    private String warehouseName;
    
    /**
     * 库存状态；0,库存；1,已出库
     */
    private Integer stockStatus;
    
    /**
     * 柜机中电池型号统计
     */
    private Map<String, Long> batteryTypeMapes;
    
    /**
     * 柜机中可换电电池型号统计
     */
    private Map<String,Integer> exchangebleMapes;
    
    /**
     * iot连接模式  0:阿里云 1：华为云 2:自建TCP
     */
    private Integer pattern;
    
    //全天
    public static final String ALL_DAY = "-1";
    //自定义时间段
    public static final String CUSTOMIZE_TIME = "1";
    //不合法数据
    public static final String ILLEGAL_DATA = "2";
    
    //营业
    public static final Integer IS_BUSINESS  = 0;
    //打烊
    public static final Integer IS_NOT_BUSINESS  = 1;
    
}
