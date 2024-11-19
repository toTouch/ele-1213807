/**
 *  Create date: 2024/6/26
 */

package com.xiliulou.electricity.constant;

/**
 * description: 微信url常量
 *
 * @author caobotao.cbt
 * @date 2024/6/26 16:13
 */
public interface WechatUrlConstant {
    
    
    String WECHAT_OAUTH2_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
    
}