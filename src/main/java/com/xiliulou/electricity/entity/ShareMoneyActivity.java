package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 活动表(ShareMoneyActivity)实体类
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_share_money_activity")
public class ShareMoneyActivity {

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
    * 活动说明
    */
    private String description;
    /**
     * 创建人uid
     */
    private Long uid;
    /**
     * 创建人用户名
     */
    private String userName;
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
     * 加盟商Id
     */
    private Integer franchiseeId;

    /**
     * 租户
     */
    private Integer tenantId;

    /**
     * 小时
     */
    private Integer hours;

    /**
     * 优惠类型，1--减免券，2--打折券，3-天数
     */
    private Integer discountType;


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


}
