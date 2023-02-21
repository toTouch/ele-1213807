package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zgw
 * @date 2023/2/21 15:02
 * @mood
 */
@Data
public class UserNotifyVo {
    
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
    
    private Long updateTime;
}
