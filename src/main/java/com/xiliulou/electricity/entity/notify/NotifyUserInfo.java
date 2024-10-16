package com.xiliulou.electricity.entity.notify;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author maxiaodong
 * @date 2024/6/19 11:20
 * @desc 公众号 用户信息
 */

@Data
@TableName("t_notify_user_info")
public class NotifyUserInfo {
    /**
     * 主键
     */
    private Long id;
    
    private String userName;
    
    private String phone;
    
    private String nickName;
    
    private String openId;
    
    private Long createTime;
    
    private Long updateTime;
    
    private Integer delFlag;
    
    private Integer isLock;
}
