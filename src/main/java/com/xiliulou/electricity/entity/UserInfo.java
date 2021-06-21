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
    private String userName;
    /**
     * 用户姓名
     */
    private String name;
    /**
     * 邮箱
     */
    private String mailbox;
    /**
     * 身份证号
     */
    private String idNumber;
    //审核状态(0--等待审核中,1--审核被拒绝,2--审核通过,3--活体检测失败,4--活体检测成功)
    private Integer authStatus;
    /**
     * 服务状态 (0--初始化,1--已实名认证)
     */
    private Integer serviceStatus;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;
    /**
     * 可用状态(0--启用，1--禁用)
     */
    private Integer usableStatus;

    private Integer tenantId;
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    //用户状态--初始化
    public static final Integer STATUS_INIT = 0;
    //已实名认证
    public static final Integer STATUS_IS_AUTH=1;


    //可用
    public static final Integer USER_USABLE_STATUS = 0;
    //禁用
    public static final Integer USER_UN_USABLE_STATUS = 1;

    //等待审核中
    public static Integer AUTH_STATUS_PENDING_REVIEW = 0;
    //审核被拒绝
    public static Integer AUTH_STATUS_REVIEW_REJECTED = 1;
    //审核通过
    public static Integer AUTH_STATUS_REVIEW_PASSED = 2;

}
