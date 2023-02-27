package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.google.gson.internal.$Gson$Types;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;

/**
 * 用户操作记录(TEleUserOperateRecord)实体类
 *
 * @author Eclair
 * @since 2022-07-12 10:10:13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_user_operate_record")
public class EleUserOperateRecord {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 操作人
     */
    private Long operateUid;

    /**
     * 被操作用户
     */
    private Long uid;

    /**
     * 操作人账号
     */
    private String name;

    /**
     * 操作模块
     */
    private Integer operateModel;

    /**
     * 操作内容
     */
    private Integer operateContent;

    /**
     * 初始月卡可用天数
     */
    private Integer oldValidDays;

    /**
     * 操作后月卡可用天数
     */
    private Integer newValidDays;

    /**
     * 初始电池编号
     */
    private String initElectricityBatterySn;
    /**
     * 操作后电池编号
     */
    private String nowElectricityBatterySn;

    /**
     * 操作前电池押金
     */
    private BigDecimal oldBatteryDeposit;

    /**
     * 操作后电池押金
     */
    private BigDecimal newBatteryDeposit;
    
    /**
     * 操作前租车押金
     */
    private BigDecimal oldCarDeposit;
    
    /**
     * 操作后租车押金
     */
    private BigDecimal newCarDeposit;
    
    /**
     * 初始车辆编号
     */
    private String initElectricityCarSn;
    
    /**
     * 操作后车辆编号
     */
    private String newElectricityCarSn;

    //租户
    private Integer tenantId;

    /**
     * 创建时间t_ele_user_operate_record
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;

    //初始使用次数
    private Long oldMaxUseCount;

    //操作后使用次数
    private Long newMaxUseCount;

    private Integer memberCardDisableStatus;

    private BigDecimal batteryServiceFee;

    public static final Integer BATTERY_MODEL = 0;
    public static final Integer DEPOSIT_MODEL = 1;
    public static final Integer MEMBER_CARD_MODEL = 2;
    
    public static final Integer CAR_DEPOSIT_MODEL = 3;
    public static final Integer CAR_MEMBER_CARD_MODEL = 4;
    public static final Integer CAR_MODEL = 5;
    
    
    public static final Integer MEMBER_CARD_EXPIRE_CONTENT = 0;
    public static final Integer DEPOSIT_EDIT_CONTENT = 1;
    public static final Integer REFUND_DEPOSIT_CONTENT = 4;
    public static final Integer BIND_BATTERY_CONTENT = 2;
    public static final Integer UN_BIND_BATTERY_CONTENT = 3;
    public static final Integer EDIT_BATTERY_CONTENT = 5;
    public static final Integer MEMBER_CARD_DISABLE = 6;
    public static final Integer CLEAN_BATTERY_SERVICE_FEE = 7;
    
    public static final Integer CAR_DEPOSIT_EDIT_CONTENT = 8;
    public static final Integer CAR_MEMBER_CARD_EXPIRE_CONTENT = 9;
    public static final Integer EDIT_CAR_CONTENT = 10;
    public static final Integer BIND_CAR_CONTENT = 11;
    
    public static final Integer UN_BIND_CAR_CONTENT = 12;
}


