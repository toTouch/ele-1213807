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
 * 停卡记录表(TEleDisableMemberCardRecord)实体类
 *
 * @author makejava
 * @since 2022-05-21 10:17:06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_disable_member_card_record")
public class EleDisableMemberCardRecord {
    /**
     * 停卡Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 月卡名称
     */
    private String memberCardName;

    /**
     * 停卡单号
     */
    private String disableMemberCardNo;

    /**
     * 停卡状态
     */
    private Integer status;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 错误原因
     */
    private String errMsg;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 租户id
     */
    private Integer tenantId;

    /**
     * 月卡剩余天数
     */
    private Long cardDays;

    /**
     * 用户选择的停卡天数
     */
    private Integer chooseDays;

    /**
     * 用户真实的停卡天数
     */
    private Integer realDays;

    /**
     * 停卡付费状态(0--初始化 1--未支付服务费，2--已支付服务费)
     */
    private Integer payStatus;

    /**
     * 电池服务费收费标准 按天计费
     */
    private BigDecimal chargeRate;

    /**
     * 停卡是否限制时间 (0--不限制，1--限制)
     */
    private Integer disableCardTimeType;

    /**
     * 停卡截止时间
     */
    private Long disableDeadline;

    public static final Integer MEMBER_CARD_NOT_DISABLE = 0;
    public static final Integer MEMBER_CARD_DISABLE = 1;
    public static final Integer MEMBER_CARD_DISABLE_REVIEW = 2;

    public static final Integer DISABLE_CARD_NOT_LIMIT_TIME = 0;
    public static final Integer DISABLE_CARD_LIMIT_TIME = 1;


}
