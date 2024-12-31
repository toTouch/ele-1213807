package com.xiliulou.electricity.entity;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xiliulou.core.json.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
    private Integer triggerCount;
    /**
    * 优惠券id
    */
    private Integer couponId;
    /**
    * 0--正常 1--删除
    */
    private Integer delFlag;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 修改时间
    */
    private Long updateTime;

    /**
     * 租户
     */
    private Integer tenantId;
    
    /**
     * 优惠券数组列
     */
    private String couponArrays;
    
    
    public void setCoupons(List<Long> couponArrays,Integer couponId){
        Set<Long> couponIds = new HashSet<>();
        if (Objects.nonNull(couponId)){
            couponIds.add(Long.valueOf(couponId));
        }
        if (CollectionUtil.isNotEmpty(couponArrays)){
            couponIds.addAll(couponArrays);
        }
        this.couponArrays = JsonUtil.toJson(couponIds);
    }
    
    public List<Long> getCoupons(){
        Set<Long> resultSet = new HashSet<>();
        if (Objects.nonNull(couponArrays)){
            resultSet.addAll(JsonUtil.fromJsonArray(couponArrays,Long.class));
        }
        if (Objects.nonNull(couponId)){
            resultSet.add(Long.valueOf(couponId));
        }
        return new ArrayList<>(resultSet);
    }

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
