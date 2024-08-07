package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 用户绑定列表(ElectricityConfig)实体类
 *
 * @author makejava
 * @since 2020-12-08 14:17:59
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_config")
public class ElectricityConfig {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 平台名称
     */
    private String name;
    
    /**
     * 订单间隔时间
     */
    private Integer orderTime;
    
    /**
     * 实名审核方式审核（0:人工审核 ,1:自动审核,2:人脸核身）
     */
    private Integer isManualReview;
    
    /**
     * 是否线上提现（0--是，1--否）
     */
    private Integer isWithdraw;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    //租户id
    private Integer tenantId;
    
    /**
     * 是否开启异常仓锁仓 （0--开启，1--关闭）
     */
    private Integer isOpenDoorLock;
    
    /**
     * 是否电池检测 （0--是，1--否）
     */
    private Integer isBatteryReview;
    
    /**
     * 是否开始暂停月卡功能 （0--关闭 1--开启）
     */
    private Integer disableMemberCard;
    
    /**
     * 是否开启低电量换电 （0--是 1--否）
     */
    private Integer isLowBatteryExchange;
    
    /**
     * 是否开启选仓换电 （0--开启 1--关闭）
     */
    private Integer isSelectionExchange;
    
    /**
     * 低电量换电模式
     */
    private String lowBatteryExchangeModel;
    
    /**
     * 是否可以自主开仓
     */
    private Integer isEnableSelfOpen;
    
    /**
     * 是否开启只剩一个格挡退电池
     */
    private Integer isEnableReturnBoxCheck;
    
    /**
     * 是否开启保险 （0--是 1--否）
     */
    private Integer isOpenInsurance;
    
    /**
     * 押金缴纳类型 (0：缴纳押金，1：电池免押金，2：租车免押金，3：车辆电池免押金)
     */
    private Integer freeDepositType;
    
    
    /**
     * 是否迁移加盟商 1--关闭 2--开启
     */
    @Deprecated
    private Integer isMoveFranchisee;
    
    /**
     * 迁移加盟商
     */
    @Deprecated
    private String franchiseeMoveInfo;
    
    /**
     * 是否启用0元退押审核 (0--是 1--否)
     */
    private Integer isZeroDepositAuditEnabled;
    
    /**
     * 是否打开车电关联 0--是 1--否
     */
    private Integer isOpenCarBatteryBind;
    
    /**
     * 是否开启车辆控制 0--是 0--否
     */
    private Integer isOpenCarControl;
    
    /**
     * 是否开启电子签名 0--是 1--否
     */
    private Integer isEnableEsign;
    
    /**
     * 是否允许租电 0--是  1--否
     */
    private Integer allowRentEle;
    
    /**
     * 是否允许退电 0--是  1--否
     */
    private Integer allowReturnEle;
    
    /**
     * 冻结是否强制退资产 0--是 1--否
     */
    private Integer allowFreezeWithAssets;
    
    /**
     * 渠道时限
     */
    private Integer channelTimeLimit;
    
    /**
     * 打开微信客服 0-是 1-否
     */
    private Integer wxCustomer;
    
    /**
     * 柜机少电比例
     */
    private BigDecimal lowChargeRate;
    
    /**
     * 柜机多电比例
     */
    private BigDecimal fullChargeRate;
    
    /**
     * 柜机少电多电配置标准:1-统一配置 2-单个柜机配置
     */
    private Integer chargeRateType;
    
    /**
     * 是否舒适换电，默认0关闭，1是开启
     */
    private Integer isComfortExchange;
    
    /**
     * 优先换电标准
     */
    private Double priorityExchangeNorm;
    
    
    public static Integer MOVE_FRANCHISEE_CLOSE = 1;
    
    public static Integer MOVE_FRANCHISEE_OPEN = 0;
    
    //人工审核
    //实名审核方式 0:人工审核 ,1:自动审核,2:人脸核身
    public static Integer MANUAL_REVIEW = 0;
    
    public static Integer AUTO_REVIEW = 1;
    
    public static Integer FACE_REVIEW = 2;
    
    
    //线上提现
    public static Integer WITHDRAW = 0;
    
    //线下提现
    public static Integer NON_WITHDRAW = 1;
    
    public static Integer OPEN_DOOR_LOCK = 0;
    
    public static Integer NON_OPEN_DOOR_LOCK = 1;
    
    public static Integer BATTERY_REVIEW = 0;
    
    public static Integer NON_BATTERY_REVIEW = 1;
    
    public static Integer NOT_DISABLE_MEMBER_CARD = 0;
    
    public static Integer DISABLE_MEMBER_CARD = 1;
    
    public static Integer LOW_BATTERY_EXCHANGE = 0;
    
    public static Integer NOT_LOW_BATTERY_EXCHANGE = 1;
    
    /**
     * 0--开启 1--关闭
     */
    public static Integer ENABLE_SELF_OPEN = 0;
    
    public static Integer DISABLE_SELF_OPEN = 1;
    
    /**
     * 是否开启只剩一个格挡退电池 0--开启 1--关闭
     */
    public static Integer ENABLE_RETURN_BOX_CHECK = 0;
    
    public static Integer DISABLE_RETURN_BOX_CHECK = 1;
    
    
    /**
     * 是否开启保险 0--开启 1--关闭
     */
    public static Integer ENABLE_INSURANCE = 0;
    
    public static Integer DISABLE_INSURANCE = 1;
    
    /**
     * 免押押金类型，0：缴纳押金，1：电池免押金，2：租车免押金，3：车辆电池免押金
     */
    public static Integer FREE_DEPOSIT_TYPE_DEFAULT = 0;
    
    public static Integer FREE_DEPOSIT_TYPE_BATTERY = 1;
    
    public static Integer FREE_DEPOSIT_TYPE_CAR = 2;
    
    public static Integer FREE_DEPOSIT_TYPE_ALL = 3;
    
    /**
     * 车电关联 0--是 1--否
     */
    public static Integer ENABLE_CAR_BATTERY_BIND = 0;
    
    public static Integer DISABLE_CAR_BATTERY_BIND = 1;
    
    /**
     * 车辆控制
     */
    public static Integer ENABLE_CAR_CONTROL = 0;
    
    public static Integer DISABLE_CAR_CONTROL = 1;
    
    /**
     * 0元退押审核 0--是 1--否
     */
    public static Integer ENABLE_ZERO_DEPOSIT_AUDIT = 0;
    
    public static Integer DISABLE_ZERO_DEPOSIT_AUDIT = 1;
    
    /**
     * 是否允许租电 0--是  1--否
     */
    public static Integer ALLOW_RENT_ELE = 0;
    
    public static Integer NOT_ALLOW_RENT_ELE = 1;
    
    /**
     * 是否允许退电 0--是  1--否
     */
    public static Integer ALLOW_RETURN_ELE = 0;
    
    public static Integer NOT_ALLOW_RETURN_ELE = 1;
    
    
    /**
     * 冻结套餐强制资产开关 0--是 1--否
     */
    public static Integer ALLOW_FREEZE_ASSETS = 0;
    
    public static Integer NOT_ALLOW_FREEZE_ASSETS = 1;
    
    /**
     * 打开微信客服 0-是 1-否
     */
    public static Integer OPEN_WX_CUSTOMER = 0;
    
    public static Integer CLOSE_WX_CUSTOMER = 1;
    
    /**
     * 柜机少电多电配置标准:0-统一配置
     */
    public static Integer CHARGE_RATE_TYPE_UNIFY = 0;
    
    /**
     * 柜机少电多电配置标准:1-单个柜机配置
     */
    public static Integer CHARGE_RATE_TYPE_SINGLE = 1;
    
    /**
     * 舒适换电打开
     */
    public static Integer COMFORT_EXCHANGE = 1;
}
