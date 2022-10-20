package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (VersionNotification)实体类
 *
 * @author Eclair
 * @since 2021-09-26 14:36:01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_version_notification")
public class VersionNotification {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 版本号
     */
    private String version;
    /**
     * 版本内容
     */
    private String content;
    /**
     * 是否发送邮件，0：未发送，1：已发送
     */
    private Integer sendMailStatus;
    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    //是否发送邮件，0：未发送，1：已发送
    public static final Integer STATUS_SEND_MAIL_NO = 0;
    public static final Integer STATUS_SEND_MAIL_YES = 1;

}
