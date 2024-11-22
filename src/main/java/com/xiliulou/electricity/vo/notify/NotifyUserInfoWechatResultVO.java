/**
 * Create date: 2024/6/26
 */

package com.xiliulou.electricity.vo.notify;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * description: 运维设置微信openid
 *
 * @author caobotao.cbt
 * @date 2024/6/26 17:16
 */
@Data
public class NotifyUserInfoWechatResultVO {
    
    /**
     * openId
     */
    private String openid;
    
    /**
     * 接口凭证
     */
    private String accessToken;
    
    
    private String errcode;
    
    private String errmsg;
}
