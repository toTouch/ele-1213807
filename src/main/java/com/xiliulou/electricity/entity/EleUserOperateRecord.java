package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
     * 初始月卡过期时间
     */
    private Long oldMemberCardExpireTime;

    /**
     * 操作后月卡过期时间
     */
    private Long newMemberCardExpireTime;

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

    //租户
    private Integer tenantId;

    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;

    public static final Integer BATTERY_MODEL = 0;
    public static final Integer DEPOSIT_MODEL = 1;
    public static final Integer MEMBER_CARD_MODEL = 2;

    public static final Integer MEMBER_CARD_EXPIRE_CONTENT = 0;
    public static final Integer DEPOSIT_EDIT_CONTENT = 1;
    public static final Integer BIND_BATTERY_CONTENT = 2;
    public static final Integer UN_BIND_BATTERY_CONTENT = 3;

}


