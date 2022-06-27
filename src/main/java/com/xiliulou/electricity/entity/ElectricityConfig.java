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
     * 是否人工审核（0--是，1--否）
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

    //人工审核
    public static Integer MANUAL_REVIEW = 0;
    //自动审核
    public static Integer AUTO_REVIEW = 1;


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

}
