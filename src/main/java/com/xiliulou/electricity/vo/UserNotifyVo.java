package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.query.NotifyPictureInfo;
import lombok.Data;

import java.util.List;

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
    
    /**
     * 0：文字 1：图片
     */
    private Integer type;
    
    private List<NotifyPictureInfo> pictureInfoList;
    
    private Long updateTime;
}
