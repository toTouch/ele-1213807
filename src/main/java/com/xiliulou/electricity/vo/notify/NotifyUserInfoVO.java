/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/26
 */

package com.xiliulou.electricity.vo.notify;

import lombok.Data;

/**
 * description: 通知用户vo
 *
 * @author caobotao.cbt
 * @date 2024/6/26 17:16
 */
@Data
public class NotifyUserInfoVO {
    
    /**
     * 主键id
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 电话
     */
    private String phone;
    
    /**
     * 昵称
     */
    private String nickName;
    
    /**
     * 微信授权用户唯一标识
     */
    private String openId;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 修改时间
     */
    private Long updateTime;
}
