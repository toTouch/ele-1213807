/**
 * Create date: 2024/6/26
 */

package com.xiliulou.electricity.service.notify;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.notify.NotifyUserInfo;
import com.xiliulou.electricity.request.notify.NotifyUserInfoOptRequest;

/**
 * description: 用户公众号通知service
 *
 * @author caobotao.cbt
 * @date 2024/6/26 14:52
 */
public interface NotifyUserInfoService {
    
    
    /**
     * 获取微信openid
     *
     * @param code
     * @author caobotao.cbt
     * @date 2024/6/26 15:57
     */
    R queryWechatOpenIdByCode(String code);
    
    /**
     * 新增
     *
     * @param request
     * @return
     * @author caobotao.cbt
     * @date 2024/6/26 15:06
     */
    R insert(NotifyUserInfoOptRequest request);
    
    
    /**
     * 更新
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/6/26 15:45
     */
    R update(NotifyUserInfoOptRequest request);
    
    
    /**
     * 根据openId 查询
     *
     * @param openId
     * @author caobotao.cbt
     * @date 2024/6/26 17:23
     */
    R queryByOpenIdFromCache(String openId);
    
    
    /**
     * 根据电话查询
     *
     * @param phone
     * @author caobotao.cbt
     * @date 2024/6/26 18:31
     */
    R queryByPhoneFromCache(String phone);
    
    
    /**
     * 查询全部
     *
     * @param offset
     * @param size
     * @author caobotao.cbt
     * @date 2024/6/26 17:35
     */
    R queryAll(Integer offset, Integer size);
    
    /**
     * 根据电话查缓存
     *
     * @param phone
     * @author caobotao.cbt
     * @date 2024/6/27 16:04
     */
    NotifyUserInfo queryFromCacheByPhone(String phone);
}