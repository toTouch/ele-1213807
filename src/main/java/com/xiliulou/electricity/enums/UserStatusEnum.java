package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author HeYafeng
 * @description 用户状态相关
 * @date 2024/12/4 17:37:55
 */

@Getter
@AllArgsConstructor
public enum UserStatusEnum {
    USER_STATUS_DELETED(1, "已删除"),
    USER_STATUS_CANCELLING(2, "注销中"),
    USER_STATUS_CANCELLED(3, "已注销"),
    
    USER_STATUS_REQUEST_DELETED(2, "已删除"),
    USER_STATUS_REQUEST_CANCELLED(3, "已注销"),
    
    USER_STATUS_VO_COMMON(0, "正常"),
    USER_STATUS_VO_DELETED(1, "已删除"),
    USER_STATUS_VO_CANCELLED(2, "已注销"),
    
    USER_DELAY_DAY_30(30, "延迟30天注销");
    
    private final Integer code;
    
    private final String desc;
    
}
