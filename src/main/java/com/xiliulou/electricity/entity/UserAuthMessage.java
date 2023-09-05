package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (UserAuthMessage)实体类
 *
 * @author Eclair
 * @since 2023-09-05 14:36:03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_auth_message")
public class UserAuthMessage {
    /**
     * 主键
     */
    private Long id;
    /**
     * 用户id
     */
    private Long uid;

    private Integer authStatus;
    /**
     * 实名审核拒绝原因
     */
    private String msg;

    private Integer tenantId;

    private Long createTime;

    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
