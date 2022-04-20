package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

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
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
