package com.xiliulou.electricity.vo;

import lombok.Data;

@Data
public class NotifyPictureInfoVO {
    /**
     * 通知图片
     */
    private String pictureUrl;
    
    /**
     * 通知图片Oss存储URL
     */
    private String pictureOSSUrl;
    
    /**
     * 通知图片的活动类型
     * 0:邀请返券
     * 1:邀请返现
     * 如果为空，则通知图片不跳转
     */
    private Integer activityType;
}
