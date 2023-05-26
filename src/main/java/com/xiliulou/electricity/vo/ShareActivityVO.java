package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.entity.ElectricityCabinetFile;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.entity.ShareActivityRule;
import com.xiliulou.electricity.query.ShareActivityRuleQuery;
import lombok.Data;

import java.util.List;

/**
 * 活动表(Activity)实体类
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
@Data
public class ShareActivityVO {

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
    * 页面横幅文件名(banner)
    */
    private String pageBannerImageName;
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
    /**
     * 活动类型，分为 1--自营，2--代理
     */
    private Integer type;
    /**
     * 领取方式 0--阶梯，1--循环
     */
    private Integer receiveType;
    /**
     * 小时
     */
    private Integer hours;

    //优惠券列表
    private List<CouponVO> couponVOList;

    /**
     * 活动图片url
     */
    private List<ElectricityCabinetFile> electricityCabinetFiles;

    //邀请人数
    private Integer count;

    //领劵次数
    private Integer couponCount;

    /**
     * 优惠类型，1--减免券，2--打折券，3-天数
     */
    private Integer discountType;

    /**
     * 可用邀请人数
     */
    private Integer availableCount;


    List<ShareActivityRule> shareActivityRuleQueryList;

    /**
     * 领券套餐
     */
    private List<ElectricityMemberCard> memberCards;



}
