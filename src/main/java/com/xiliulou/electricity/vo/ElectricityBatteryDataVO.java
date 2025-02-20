package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.enums.battery.BatteryLabelEnum;
import com.xiliulou.electricity.vo.battery.ElectricityBatteryLabelVO;
import lombok.Data;

@Data
public class ElectricityBatteryDataVO {

    private Long id;
    /**
     * sn码
     */
    private String sn;
    /**
     * 电池型号
     */
    private String model;
    /**
     * 电池电量
     */
    
    private Double power;
    /**
     * 电压
     */
    
    private Integer voltage;
    /**
     * 电池容量,单位(mah)
     */
    
    private Integer dbCapacity;
    /**
     * 电池状态 0：在仓，1：在库，2：租借，3：异常取走，4游离
     */
    
    private Integer status;
    /**
     * 电池物理状态 0：在仓，1：不在仓
     */
    private Integer physicsStatus;
    /**
     * 电池业务状态：1：已录入，2：租借，3：归还，4：异常交换
     */
    private Integer businessStatus;

    private Long createTime;

    private Long updateTime;

    private Long borrowExpireTime;

    private Integer delFlag;
    
    /**
     * 异常交换的电池用户id
     */
    private Long guessUid;
    
    /**
     * 异常交换的电池用户名称
     */
    private String guessUserName;
    
    /**
     * 异常交换的电池用户手机号
     */
    private String guessUserPhone;
    
    /**
     * 0：正常 1：故障
     */
    
    private Integer healthStatus;
    /**
     * 0--空闲 1--正在开机 2--充电中 3--充满电 4--限额充电 -1 未充电
     */
    
    private Integer chargeStatus;

    /**
     * 地址经度
     */
    
    private Double longitude;
    /**
     * 地址纬度
     */
    
    private Double latitude;

    //租户id
    private Integer tenantId;

    /**
     * 所属换电柜柜Id
     */
    private Integer electricityCabinetId;
    private String  electricityCabinetName;

    /**
     * 所属用户id
     */
    private Long uid;

    /**
     * 电池上次所在的舱门号
     */
//    private String lastDepositCellNo;

    /**
     * 物联网卡号
     */
    private String iotCardNumber;

    /**
     * 换电次数
     */
    private Integer exchangeCount;
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 过期时间
     */
    private Long memberCardExpireTime;
    
    /**
     * 电池标签
     * @see BatteryLabelEnum
     */
    private Integer label;
    
    /**
     * 电池标签关联数据
     */
    private ElectricityBatteryLabelVO labelVO;


    /**
     * 删除状态 0：正常，1：删除
     */
    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;
    /**
     * 电池物理状态 0：在仓，1：不在仓
     */
    public static final Integer PHYSICS_STATUS_WARE_HOUSE = 0;
    public static final Integer PHYSICS_STATUS_NOT_WARE_HOUSE = 1;
    /**
     * 电池业务状态：1：已录入，2：租借，3：归还，4：异常交换
     */
    public static final Integer BUSINESS_STATUS_INPUT = 1;
    public static final Integer BUSINESS_STATUS_LEASE = 2;
    public static final Integer BUSINESS_STATUS_RETURN = 3;
    public static final Integer BUSINESS_STATUS_EXCEPTION = 4;

//    //在仓
//    public static final Integer WARE_HOUSE_STATUS = 0;
//    //在库
//    public static final Integer STOCK_STATUS = 1;
//    //租借
//    public static final Integer LEASE_STATUS = 2;
//    //异常取走
//    public static final Integer EXCEPTION_STATUS = 3;
//    //游离
//    public static final Integer EXCEPTION_FREE = 4;


    //柜机上报
    public static final Integer REPORT_TYPE_ELECTRICITY_CABINET = 0;
    //电池上报
    public static final Integer REPORT_TYPE_ELECTRICITY_BATTERY = 1;

    /**
     * 仓门号
     */
    private String cellNo;

    private String franchiseeName;

    private String phone;

    private String name;

    private String userName;

    /**
     * 柜机上报电压
     */

    private Integer boxVoltage;
}
