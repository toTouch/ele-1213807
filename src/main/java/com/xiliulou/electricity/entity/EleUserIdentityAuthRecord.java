package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Kenneth
 * @Date: 2023/7/9 18:13
 * @Description:
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_user_identity_auth_record")
public class EleUserIdentityAuthRecord {

    private Long id;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 个人账号ID 来源于E签宝
     */
    private String psnId;

    /**
     * 个人标识账号 (手机号/邮箱)
     */
    private String psnAccount;

    /**
     * 认证流程ID
     */
    private String authFlowId;

    /**
     * 实名状态 0-未实名，1-已实名
     */
    private Integer realNameStatus;

    /**
     * 是否删除（0-正常，1-删除）
     */
    private Integer delFlag;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 修改时间
     */
    private Long updateTime;

}
