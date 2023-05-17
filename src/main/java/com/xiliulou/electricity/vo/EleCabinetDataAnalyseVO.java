package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-18-14:05
 */
@Data
public class EleCabinetDataAnalyseVO {

    /**
     * 换电柜Id
     */
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
     * 可用状态(0--启用，1--禁用)
     */
    private Integer usableStatus;
    /**
     * 在线状态（0--连网，1--断网）
     */
    private Integer onlineStatus;
    /**
     * 型号Id
     */
    private Integer modelId;
    /**
     * 满电标准
     */
    private Double fullyCharged;
    /**
     * 型号Id
     */
    private String modelName;
    /**
     * 版本
     */
    private String version;
    /**
     * 二维码
     */
    private String QRCode;
    /**
     * 加盟商
     */
    private String franchiseeName;
    /**
     * 加盟商ID
     */
    private Long franchiseeId;
    /**
     * 门店
     */
    private String storeName;
    private Long storeId;
    /**
     * 电柜温度
     */
    private double temp;
    /**
     * 可换电电池个数
     */
    private Integer exchangeBatteryNumber;
    /**
     * 满电电池个数
     */
    private Integer fullBatteryNumber;

    /**
     * 充电电池个数
     */
    private Integer chargeBatteryNumber;

    /**
     * 空仓数
     */
    private Integer emptyCellNumber;

    /**
     * 禁用仓数
     */
    private Integer disableCellNumber;
    /**
     * 加热仓数
     */
    private Integer heatingCellNumber;

    /**
     * 散热仓数
     */
    private Integer fanOpenNumber;

    /**
     * 电表读数
     */
    private Double powerConsumption;

    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;
    /**
     * 服务到期时间
     */
    private Long serviceEndTime;

    private Integer tenantId;
}
