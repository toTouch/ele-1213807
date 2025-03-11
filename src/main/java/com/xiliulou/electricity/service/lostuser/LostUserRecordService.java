package com.xiliulou.electricity.service.lostuser;

import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.security.bean.TokenUser;

/**
 * 流失用户记录(TLostUserRecord)表服务接口
 *
 * @author maxiaodong
 * @since 2024-10-09 11:39:08
 */

public interface LostUserRecordService {
    
    void doLostUser(TokenUser user, UserInfoExtra userInfoExtra, long prospectTime, Integer packageType, String orderId);
}
