package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 活动表(Activity)实体类
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
@Data
public class NewUserActivityAddAndUpdateQuery {

    @NotNull(message = "活动id不能为空!", groups = {UpdateGroup.class})
    private Integer id;
    /**
    * 活动名称
    */
    @NotEmpty(message = "活动名称不能为空!", groups = {CreateGroup.class})
    private String name;

    /**
     * 奖励类型，1--次数，2--优惠券
     */
    @NotNull(message = "奖励类型不能为空!", groups = {CreateGroup.class})
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
     * 时间类型，分为 1--有限制，2--无限制
     */
    @NotNull(message = "时间类型不能为空!", groups = {CreateGroup.class})
    private Integer timeType;

    /**
     * 活动开始时间
     */
    private Integer beginTime;
    /**
     * 活动结束时间
     */
    private Integer endTime;
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
    private Integer delFlg;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 修改时间
    */
    private Long updateTime;

}


