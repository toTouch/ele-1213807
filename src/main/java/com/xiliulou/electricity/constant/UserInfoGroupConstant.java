package com.xiliulou.electricity.constant;

/**
 * @author HeYafeng
 * @description 用户分组相关常量
 * @date 2024/4/11 18:41:02
 */
public interface UserInfoGroupConstant {
    
    /**
     * 用户绑定的分组数量限制
     */
    int USER_GROUP_LIMIT = 10;
    
    /**
     * 用户分组记录来源:其它
     */
    int USER_GROUP_HISTORY_TYPE_OTHER = 0;
    
    /**
     * 用户分组记录来源:退押
     */
    int USER_GROUP_HISTORY_TYPE_REFUND_DEPOSIT = 1;
    
    String USER_GROUP_HISTORY_TYPE_REFUND_DEPOSIT_NAME = "用户退押";
}
