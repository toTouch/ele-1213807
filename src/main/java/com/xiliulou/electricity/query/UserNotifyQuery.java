package com.xiliulou.electricity.query;

import lombok.Data;

import java.util.List;

/**
 * @author zgw
 * @date 2023/2/21 9:35
 * @mood
 */
@Data
public class UserNotifyQuery {
    
    /**
     * 通知状态 1--关闭 0--开启
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
    
    /**
     * 0：文字 1：图片
     */
    private Integer type;
    
    private List<NotifyPictureInfo> pictureInfoList;
    
}
