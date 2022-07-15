package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 动态验证码(VerificationCode)实体类
 *
 * @author Eclair
 * @since 2022-06-28 11:07:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_verification_code")
public class VerificationCode {
    /**
     * 主键
     */
    private Long id;
    /**
     * 验证码
     */
    private String verificationCode;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 状态;0：启用，1：禁用
     */
    private Integer status;
    /**
     * 是否删除;0：正常，1：删除
     */
    private Integer delFlag;
    /**
     * 创建时间
     */
    private Long createdTime;
    /**
     * 更新时间
     */
    private Long updatedTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;


    public static final Integer STATUE_ENABLE = 0;
    public static final Integer STATUE_DISABLE = 1;



}
