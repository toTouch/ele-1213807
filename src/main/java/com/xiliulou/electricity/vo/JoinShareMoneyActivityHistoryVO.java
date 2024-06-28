package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.enums.ActivityEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)实体类
 *
 * @author Eclair
 * @since 2021-07-14 09:44:36
 */
@Data
public class JoinShareMoneyActivityHistoryVO {

    private Long id;

    /**
     * recordId
     */
    private Long recordId;
    //    /**
    //    * 邀请用户uid
    //    */
    //    private Long uid;
    //    /**
    //    * 参与用户uid
    //    */
    //    private Long joinUid;
    /**
     * 参与用户名
     */
    private String joinName;
    /**
     * 参与用户phone
     */
    private String joinPhone;
    /**
    * 参与开始时间
    */
    private Long startTime;
    /**
    * 参与过期时间
    */
    private Long expiredTime;
    /**
    * 参与状态 1--初始化，2--已参与，3--已过期，4--被替换
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

    /**
     * 活动id
     */
    private Integer activityId;
    /**
     * 返现金额
     */
    private BigDecimal money;

    /**
     * 邀请标准 0-登录注册 1-实名认证 2-购买套餐
     * @see ActivityEnum
     */
    private Integer invitationCriteria;

    /**
     * 邀请人UID
     */
    private Long inviterUid;

    /**
     * 邀请人姓名
     */
    private String inviterName;

    /**
     * 邀请人电话
     */
    private String inviterPhone;

    /**
     * 活动名称
     */
    private String activityName;
    
    private Long franchiseeId;
    
    private String franchiseeName;

    //初始化
    public static Integer STATUS_INIT = 1;
    //已参与
    public static Integer STATUS_SUCCESS = 2;
    //已过期
    public static Integer STATUS_FAIL = 3;
    //被替换
    public static Integer STATUS_REPLACE = 4;

}
