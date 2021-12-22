package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 参与邀请活动记录(JoinShareMoneyActivityRecord)实体类
 *
 * @author Eclair
 * @since 2021-07-14 09:44:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_join_share_money_activity_record")
public class JoinShareMoneyActivityRecord {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    /**
     * 活动id
     */
    private Integer activityId;
    /**
    * 邀请用户uid
    */
    private Long uid;
    /**
    * 参与用户uid
    */
    private Long joinUid;
    /**
    * 参与开始时间
    */
    private Long startTime;
    /**
    * 参与过期时间
    */
    private Long expiredTime;
    /**
    * 参与状态 1--初始化，2--已参与，3--已过期  4--活动已下架
    */
    private Integer status;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 修改时间
    */
    private Long updateTime;
    /**
    * 租户id
    */
    private Integer tenantId;


    //初始化
    public static Integer STATUS_INIT = 1;
    //已参与
    public static Integer STATUS_SUCCESS = 2;
    //已过期
    public static Integer STATUS_FAIL = 3;

    //活动已下架
    public static Integer STATUS_OFF = 5;

}
