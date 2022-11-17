package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 启用套餐(TEnableMemberCardRecord)实体类
 *
 * @author makejava
 * @since 2022-11-17 16:00:45
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_cabinet_order")
public class EnableMemberCardRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 套餐名称
     */
    private String memberCardName;

    /**
     * 启用类型 0--系统启用 1--人为启用
     */
    private Integer enableType;

    /**
     * 停卡天数
     */
    private Integer disableDays;

    /**
     * 电池服务费状态(。0--初始化 1--未支付服务费，2--已支付服务费)
     */
    private Integer batteryServiceFeeStatus;

    /**
     * 停卡单号
     */
    private String disableMemberCardNo;

    /**
     * 用户Id
     */
    private Long uid;

    /**
     * 停卡时间
     */
    private Long disableTime;

    /**
     * 启用时间
     */
    private Long enableTime;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;
    //租户id
    private Integer tenantId;

    /**
     * 加盟商Id
     */
    private Integer franchiseeId;




}
