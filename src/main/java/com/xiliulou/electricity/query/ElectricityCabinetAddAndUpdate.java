package com.xiliulou.electricity.query;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 换电柜表(TElectricityCabinet)实体类
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@Data
public class ElectricityCabinetAddAndUpdate {
    /**
     * 换电柜Id
     */
    @NotNull(message = "换电柜Id不能为空!", groups = {UpdateGroup.class})
    private Integer id;
    /**
     * 换电柜名称
     */
    @NotEmpty(message = "换电柜名称不能为空!", groups = {CreateGroup.class})
    private String name;
    /**
     * 换电柜sn
     */
    @NotEmpty(message = "换电柜sn不能为空!", groups = {CreateGroup.class})
    private String sn;
    /**
     * 换电柜地址
     */
    @NotEmpty(message = "换电柜地址不能为空!", groups = {CreateGroup.class})
    private String address;
    /**
     * 地址经度
     */
    @NotNull(message = "地址经度不能为空!", groups = {CreateGroup.class})
    private Double longitude;
    /**
     * 地址纬度
     */
    @NotNull(message = "地址纬度不能为空!", groups = {CreateGroup.class})
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
    @NotNull(message = "可用状态不能为空!", groups = {CreateGroup.class})
    private Integer usableStatus;
    /**
     * 物联网连接状态（0--连网，1--断网）
     */
    private Integer onlineStatus;
    /**
     * 型号Id
     */
    @NotNull(message = "型号Id不能为空!", groups = {CreateGroup.class})
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
    @NotEmpty(message = "联系电话不能为空!", groups = {CreateGroup.class})
    private String servicePhone;
    /**
     * 营业时间类型
     */
    @NotEmpty(message = "营业时间类型不能为空!", groups = {CreateGroup.class})
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
     * 营业开始时间
     */
    private Long beginTime;
    /**
     * 营业结束时间
     */
    private Long endTime;

    //门店id
    @NotNull(message = "门店不能为空!", groups = {CreateGroup.class})
    private Long storeId;

    /**
     * 换电方式 1：有屏，2：无屏，3：单片机
     */
    @NotNull(message = "换电方式不能为空!", groups = {CreateGroup.class})
    private Integer exchangeType;
    
    /**
     * 区域id
     */
    private Long areaId;
    
    /**
     * 场地费
     */
    private BigDecimal placeFee;
    
    
    /**
     * 最小保留电池数量，不限制为null;限制则需要前端传递
     */
    @Min(value = 0, message = "最小保留电池数量不能小于0")
    @Max(value = 99, message = "最小保留电池数量不能超过99")
    private Integer minRetainBatteryCount;
    
    /**
     * 最大保留电池数量，不限制为null;限制则需要前端传递
     */
    @Min(value = 0, message = "最大保留电池数量不能小于0")
    @Max(value = 99, message = "最大保留电池数量不能超过99")
    private Integer maxRetainBatteryCount;

    //全天
    public static final String ALL_DAY = "-1";
    //自定义时间段
    public static final String CUSTOMIZE_TIME = "1";



}
