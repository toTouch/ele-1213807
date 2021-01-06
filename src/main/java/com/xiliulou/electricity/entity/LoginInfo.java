package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户绑定列表(LoginInfo)实体类
 *
 * @author makejava
 * @since 2020-12-08 14:17:59
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_login_info")
public class LoginInfo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * ip
     */
    private String ip;
    /**
     * uid
     */
    private Long uid;
    /**
     * 用户姓名
     */
    private String name;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 登录时间
     */
    private Long loginTime;
    /**
     * 登录类型(1:后台,2:用户)
     */
    private Integer type;
    /**
     * 备注
     */
    private String remark;

}