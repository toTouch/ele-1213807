package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * (UserCarMemberCard)表实体类
 *
 * @author zzlong
 * @since 2022-12-07 17:36:11
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_car_member_card")
public class UserCarMemberCard {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * uid
     */
    private Long uid;
    /**
     * 车辆套餐id
     */
    private Long cardId;
    /**
     * 车辆套餐过期时间
     */
    private Long memberCardExpireTime;
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;

    private Integer tenantId;

    private Long createTime;

    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
