package com.xiliulou.electricity.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.List;

/**
 * 加盟商活动绑定表(ShareActivityRule)实体类
 *
 * @author makejava
 * @since 2021-04-23 16:43:23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_share_activity_rule")
public class ShareActivityRule {
    /**
    * 主键Id
    */
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    /**
    * 活动id
    */
    private Integer activityId;
    /**
     * 触发人数
     */
    private Double triggerCount;
    /**
    * 优惠券id
    */
    private List<Integer> couponIds;
    /**
    * 优惠类型，1--减免券，2--打折券，3-天数
    */
    private Integer discountType;
    /**
    * 0--正常 1--删除
    */
    private Integer delFlg;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 修改时间
    */
    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
