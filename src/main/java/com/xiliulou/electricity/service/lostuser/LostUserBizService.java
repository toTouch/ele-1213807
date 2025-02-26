package com.xiliulou.electricity.service.lostuser;

import com.xiliulou.electricity.vo.merchant.MerchantInviterVO;

/**
 * 流失用户记录(TLostUserRecord)表服务接口
 *
 * @author maxiaodong
 * @since 2024-10-09 11:39:08
 */

public interface LostUserBizService {
    
    
    void checkLostUser();
    
    void updateLostUserStatusAndUnbindActivity(Integer tenantId, Long uid, MerchantInviterVO successInviterVO);
    
    void updateLostUserNotActivity(Long uid);
}
