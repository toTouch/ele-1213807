package com.xiliulou.electricity.entity;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.core.json.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 活动表(NewUserActivity)实体类
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_new_user_activity")
public class NewUserActivity {

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /**
    * 活动名称
    */
    private String name;
    /**
    * 活动状态，分为 1--上架，2--下架
    */
    private Integer status;
    /**
     * 活动类型，分为 1--自营，2--代理
     */
    private Integer type;
    /**
     * 时间类型，分为 1--有限制，2--无限制
     */
    private Integer timeType;
    /**
     * 活动开始时间
     */
    private Long beginTime;
    /**
     * 活动结束时间
     */
    private Long endTime;
    /**
     * 奖励类型，1--次数，2--优惠券
     */
    private Integer discountType;

    /**
     * 换电次数
     */
    private Integer count;

    /**
     * 有效天数
     */
    private Integer days;

    /**
     * 优惠券id
     */
    private Integer couponId;
    
    /**
     * 优惠券数组列
     */
    private String couponArrays;

    /**
    * 活动说明
    */
    private String description;
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


    /**
     * 加盟商Id
     */
    private Integer franchiseeId;

    /**
     * 租户
     */
    private Integer tenantId;

    /**
     * 创建人uid
     */
    private Long uid;
    /**
     * 创建人用户名
     */
    private String userName;
    
    
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

    //自营
    public static final Integer SYSTEM = 1;
    //代理
    public static final Integer FRANCHISEE = 2;



    //上架
    public static final Integer STATUS_ON = 1;
    //下架
    public static final Integer STATUS_OFF = 2;


    //1--次数
    public static final Integer TYPE_COUNT = 1;
    //2--优惠券
    public static final Integer TYPE_COUPON = 2;


}
