package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * 系统升级邮件通知表实体类
 *
 * @author zzlong
 * @since 2022-08-08 15:30:14
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_upgrade_notify_mail")
public class UpgradeNotifyMail {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 通知邮箱json
     */
    private String mail;

    private Long tenantId;

    private Long createTime;

    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
