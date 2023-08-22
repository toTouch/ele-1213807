package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Integer serviceStatus;
    /**
     * 可用状态(0--启用，1--禁用)
     */
    private Integer usableStatus;

    /**
     * 电池租赁状态 0--未租电池，1--已租电池
     */
    private Integer batteryRentStatus;

    /**
     * 电池押金状态 0--未缴纳押金，1--已缴纳押金,2--押金退款中
     */
    private Integer batteryDepositStatus;

    /**
     * 车辆租赁状态
     */
    private Integer carRentStatus;

    /**
     * 车辆押金状态
     */
    private Integer carDepositStatus;

    /**
     * 车电一体押金状态
     * @see com.xiliulou.electricity.enums.YesNoEnum
     */
    private Integer carBatteryDepositStatus;

    /**
     实名认证审核类型，1：人工审核，2：自动审核，3：人脸审核
     */
    private Integer authType;

    /**
     *
     */
    private Long franchiseeId;

    private Long storeId;

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

    /**
     * 套餐购买次数(所有套餐类型的总次数，包含：换电、车、车电一体)
     */
    private Integer payCount;

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
     * -1：初始化，0：等待审核中，1：审核被拒绝，2：审核通过，3：人脸核身失败
     */
    public static final Integer AUTH_STATUS_STATUS_INIT = -1;
    public static Integer AUTH_STATUS_PENDING_REVIEW = 0;
    public static Integer AUTH_STATUS_REVIEW_REJECTED = 1;
    public static Integer AUTH_STATUS_REVIEW_PASSED = 2;
    public static Integer AUTH_STATUS_FACE_FAIL = 3;


    //电池赁状态 0--未租电池，1--已租电池
    public static final Integer BATTERY_RENT_STATUS_NO = 0;
    public static final Integer BATTERY_RENT_STATUS_YES = 1;

    //电池押金状态 0--未缴纳押金，1--已缴纳押金
    public static final Integer BATTERY_DEPOSIT_STATUS_NO = 0;
    public static final Integer BATTERY_DEPOSIT_STATUS_YES = 1;


    //车辆赁状态 0--未租车辆，1--已租车辆
    public static final Integer CAR_RENT_STATUS_NO = 0;
    public static final Integer CAR_RENT_STATUS_YES = 1;


    //车辆押金状态 0--未缴纳押金，1--已缴纳押金
    public static final Integer CAR_DEPOSIT_STATUS_NO = 0;
    public static final Integer CAR_DEPOSIT_STATUS_YES = 1;


    public static final Integer RENT_BATTERY_TYPE = 0;
    public static final Integer BATTERY_DEPOSIT_TYPE = 1;

    //实名认证审核类型，1：人工审核，2：自动审核，3：人脸审核
    public static final Integer AUTH_TYPE_PERSON = 1;
    public static final Integer AUTH_TYPE_SYSTEM = 2;
    public static final Integer AUTH_TYPE_FACE = 3;

    /** 虚拟门店ID */
    public static final Long VIRTUALLY_STORE_ID = 0L;

}
