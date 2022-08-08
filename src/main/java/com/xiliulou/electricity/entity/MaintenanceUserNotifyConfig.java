package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (MaintenanceUserNotifyConfig)实体类
 *
 * @author HRP
 * @since 2022-05-19 09:07:48
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_maintenance_user_notify_config")
public class MaintenanceUserNotifyConfig {

    private Integer id;
    /**
     * 用户手机号数组json
     */
    private String phones;

    private Long createTime;

    private Long updateTime;

    private Integer tenantId;
    /**
     * 选择权限,采用二进制与的方式进行判断
     */
    private Integer permissions;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;
    /**
     * 设备上下线
     */
    public static Integer P_DEVICE = 1;

    /**
     * 用户上报异常
     */
    public static Integer TYPE_USER_UPLOAD_EXCEPTION = 3;
    /**
     * 设备消息通知
     */
    public static Integer P_HARDWARE_INFO = 0;

}
