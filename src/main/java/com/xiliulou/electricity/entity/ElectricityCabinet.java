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
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 换电柜名称
     */
    private String name;
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
     * 更新时间（兼用在线时间）
     */
    private Long updateTime;
    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;

    //租户id
    private Integer tenantId;

    //门店id
    private Long storeId;

    private Long franchiseeId;

    /**
     * 换电方式 1：有屏，2：无屏，3：单片机
     */
    private Integer exchangeType;

    //换电方式 1：有屏，2：无屏，3：单片机
    public static final Integer EXCHANGE_TYPE_SCREEN = 1;
    public static final Integer EXCHANGE_TYPE_NO_SCREEN = 2;
    public static final Integer EXCHANGE_TYPE_MICROCOMPUTER = 3;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    //可用
    public static final Integer ELECTRICITY_CABINET_USABLE_STATUS = 0;
    //禁用
    public static final Integer ELECTRICITY_CABINET_UN_USABLE_STATUS = 1;
    //连网
    public static final Integer ELECTRICITY_CABINET_ONLINE_STATUS = 0;
    //断网
    public static final Integer ELECTRICITY_CABINET_OFFLINE_STATUS = 1;

    public static final Integer STATUS_ONLINE = 0;
    public static final Integer STATUS_OFFLINE = 1;

    public static final String IOT_STATUS_ONLINE = "online";
    public static final String IOT_STATUS_OFFLINE = "offline";
    public static final String USER_UPLOAD_EXCEPTION_STATUS = "offline";

}
