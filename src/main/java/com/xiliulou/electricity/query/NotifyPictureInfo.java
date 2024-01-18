package com.xiliulou.electricity.query;

import lombok.Data;

@Data
public class NotifyPictureInfo {
    
    /**
     * 通知图片
     */
    private String pictureUrl;
    
    /**
     * 通知图片的活动类型
     * 0:邀请返券
     * 1:邀请返现
     * 如果为空，则通知图片不跳转
     */
    private Integer activityType;
}
