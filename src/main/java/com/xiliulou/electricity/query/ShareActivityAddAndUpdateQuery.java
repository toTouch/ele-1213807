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
public class ShareActivityAddAndUpdateQuery {

    @NotNull(message = "活动id不能为空!", groups = {UpdateGroup.class})
    private Integer id;
    /**
    * 活动名称
    */
    @NotEmpty(message = "活动名称不能为空!", groups = {CreateGroup.class})
    private String name;
    /**
    * 活动状态，分为 1--上架，2--下架
    */
    private Integer status;
    /**
     * 活动类型，分为 1--自营，2--代理，3--自营内部链接，4--自营外部链接，5--代理内部链接，6--代理外部链接
     */
    @NotNull(message = "活动类型不能为空!", groups = {CreateGroup.class})
    private Integer type;
    /**
    * 推荐方式 1--首页弹窗推荐，2--固定入口
    */
    @NotNull(message = "推荐方式不能为空!", groups = {CreateGroup.class})
    private Integer showWay;
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
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空!", groups = {CreateGroup.class})
    private Long startTime;
    /**
     * 结束时间
     */
    @NotNull(message = "结束时间不能为空!", groups = {CreateGroup.class})
    private Long endTime;

    //优惠券列表
    List<Integer> couponIds;


    /**
     * 加盟商Id
     */
    private Integer franchiseeId;



}
