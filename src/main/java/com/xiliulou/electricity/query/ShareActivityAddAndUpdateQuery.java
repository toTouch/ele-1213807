package com.xiliulou.electricity.query;

import com.xiliulou.electricity.enums.ActivityEnum;
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
public class ShareActivityAddAndUpdateQuery {

    @NotNull(message = "活动id不能为空!", groups = {UpdateGroup.class})
    private Integer id;
    /**
    * 活动名称
    */
    @NotEmpty(message = "活动名称不能为空!", groups = {CreateGroup.class})
    private String name;
    /**
     * 优惠类型，1--减免券，2--打折券，3-天数
     */
    @NotNull(message = "优惠类型不能为空!", groups = {CreateGroup.class})
    private Integer discountType;

    /**
     * 有效时间
     */
    @NotNull(message = "有效时间不能为空!", groups = {CreateGroup.class})
    private Integer hours;
    /**
    * 活动状态，分为 1--上架，2--下架
    */
    private Integer status;
    /**
     * 活动类型，分为 1--自营，2--代理
     */
    private Integer type;
    /**
     * 领取方式 0--阶梯，1--循环
     */
    @NotNull(message = "领取方式不能为空!", groups = {CreateGroup.class})
    private Integer receiveType;
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

    //优惠券列表
    List<ShareActivityRuleQuery> shareActivityRuleQueryList;

    /**
     * 加盟商Id
     */
    private Integer franchiseeId;

    /**
     * 可发放优惠券的套餐
     */
    private List<Long> membercardIds;

    /**
     * 邀请标准 0-登录注册 1-实名认证 2-购买套餐
     * @see ActivityEnum
     */
    private Integer invitationCriteria;

    /**
     * 换电套餐IDs
     */
    private List<Long> batteryPackages;

    /**
     * 租车套餐IDs
     */
    private List<Long> carRentalPackages;

    /**
     * 车电一体套餐IDs
     */
    private List<Long> carWithBatteryPackages;

}


