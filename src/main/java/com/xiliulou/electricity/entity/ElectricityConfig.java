package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Integer isMoveFranchisee;

    /**
     * 迁移加盟商
     */
    private String franchiseeMoveInfo;

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


}
