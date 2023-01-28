package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * (UserBatteryMemberCard)表实体类
 *
 * @author zzlong
 * @since 2022-12-06 13:38:52
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_battery_member_card")
public class UserBatteryMemberCard {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 套餐id
     */
    private Long memberCardId;

    /**
     * 套餐到期时间
     */
    private Long memberCardExpireTime;

    /**
     * 套餐剩余次数
     */
    private Integer remainingNumber;

    /**
     * 套餐状态
     */
    private Integer memberCardStatus;

    /**
     * 月卡暂停启用更新时间
     */
    private Long disableMemberCardTime;


    /**
     * 0:正常，1：删除
     */
    private Integer delFlag;

    private Integer tenantId;

    private Long createTime;

    private Long updateTime;

    /**
     * 套餐购买次数
     */
    private Integer cardPayCount;

    public static final Integer DEL_NORMAL = 0;

    public static final Integer DEL_DEL = 1;

    public static final Integer MEMBER_CARD_NOT_DISABLE = 0;
    public static final Integer MEMBER_CARD_DISABLE = 1;
    /**
     * 月卡审核中
     */
    public static final Integer MEMBER_CARD_DISABLE_REVIEW = 2;
    public static final Integer MEMBER_CARD_DISABLE_REVIEW_REFUSE = 3;

    public static final Long MEMBER_CARD_ZERO_REMAINING = 0L;


    public static final Long UN_LIMIT_COUNT_REMAINING_NUMBER = 9999L;

    public static final Integer MEMBER_CARD_OWE = 1;

    public static final Long SEND_REMAINING_NUMBER = -1L;


}
