package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

/**
 * 用户绑定列表(TUserInfo)实体类
 *
 * @author makejava
 * @since 2020-12-08 14:17:59
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_info")
public class UserInfo {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private Long uid;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 用户名
     */
    @Deprecated
    private String userName;
    
    /**
     * 用户姓名
     */
    private String name;
    
    /**
     * 身份证号
     */
    private String idNumber;
    
    //审核状态 0--等待审核中,1--审核被拒绝,2--审核通过
    private Integer authStatus;
    
    /**
     * 服务状态 (0--初始化,1--已实名认证)
     */
    @Deprecated
    //    private Integer serviceStatus;
    /**
     * 可用状态(0--启用，1--禁用)
     */
    private Integer usableStatus;
    
    /**
     * 电池租赁状态 0--未租电池，1--已租电池
     */
    private Integer batteryRentStatus;
    
    /**
     * 电池押金状态 0--未缴纳押金，1--已缴纳押金,2--押金退款中,3--押金退款失败
     */
    private Integer batteryDepositStatus;
    
    /**
     *
     */
    private Integer carRentStatus;
    
    /**
     *
     */
    private Integer carDepositStatus;
    
    /**
     *
     */
    private Long franchiseeId;
    
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    //租户
    private Integer tenantId;
    
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
    
    //用户状态--初始化
    public static final Integer STATUS_INIT = 0;
    
    //已实名认证
    public static final Integer STATUS_IS_AUTH = 1;
    
    public static final Integer STATUS_AUDIT_PASS = 2;
    
    
    //可用
    public static final Integer USER_USABLE_STATUS = 0;
    
    //禁用
    public static final Integer USER_UN_USABLE_STATUS = 1;
    
    /**
     * 用户状态 扫码登录成功未开始实名认证
     */
    public static final Integer AUTH_STATUS_STATUS_INIT = -1;
    
    //等待审核中
    public static Integer AUTH_STATUS_PENDING_REVIEW = 0;
    
    //审核被拒绝
    public static Integer AUTH_STATUS_REVIEW_REJECTED = 1;
    
    //审核通过
    public static Integer AUTH_STATUS_REVIEW_PASSED = 2;
    
    
    
    //电池赁状态 0--未租电池，1--已租电池
    public static final Integer BATTERY_RENT_STATUS_NO = 0;
    public static final Integer BATTERY_RENT_STATUS_YES = 1;
    
    //押金状态 0--未缴纳押金，1--已缴纳押金，2--押金退款中,3--押金退款失败
    public static final Integer BATTERY_DEPOSIT_STATUS_NO = 0;
    public static final Integer BATTERY_DEPOSIT_STATUS_YES = 1;
    public static final Integer BATTERY_DEPOSIT_STATUS_REFUNDING = 2;
    @Deprecated
    public static final Integer BATTERY_DEPOSIT_STATUS_REFUND_FAIL = 3;
    
}
