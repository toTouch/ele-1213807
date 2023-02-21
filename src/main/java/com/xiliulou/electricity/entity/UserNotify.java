package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (UserNotify)实体类
 *
 * @author Eclair
 * @since 2023-02-21 09:10:39
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_notify")
public class UserNotify {
    
    private Long id;
    
    /**
     * 通知状态 0--关闭 1--开启
     */
    private Integer status;
    
    /**
     * 通知开始时间
     */
    private Long beginTime;
    
    /**
     * 通知结束时间
     */
    private Long endTime;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 内容
     */
    private String content;
    
    private Long createTime;
    
    private Long updateTime;
    
    private Integer tenantId;
    
    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;
    
    public static final Integer STATUS_OFF = 0;
    
    public static final Integer STATUS_ON = 1;
}
