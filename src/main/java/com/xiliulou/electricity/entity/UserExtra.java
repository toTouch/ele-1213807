package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (UserExtra)实体类
 *
 * @author Eclair
 * @since 2023-07-03 15:08:23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_extra")
public class UserExtra {

    private Long uid;
    /**
     * 用户来源，1：扫码，2：邀请，3：其它
     */
    private Integer source;

    private Long eid;
    /**
     * 邀请人
     */
    private Long inviter;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;

    private Integer tenantId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    //用户来源，1：扫码，2：邀请，3：其它
    public static final Integer SOURCE_TYPE_SCAN = 1;
    public static final Integer SOURCE_TYPE_INVITE = 2;
    public static final Integer SOURCE_TYPE_ONLINE = 3;
}
