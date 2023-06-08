package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (ShareActivityMemberCard)实体类
 *
 * @author Eclair
 * @since 2023-05-24 10:19:26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_share_activity_member_card")
public class ShareActivityMemberCard {

    private Long id;
    /**
     * 活动Id
     */
    private Long activityId;
    /**
     * 套餐Id
     */
    private Long memberCardId;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;
    /**
     * 租户id
     */
    private Integer tenantId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
