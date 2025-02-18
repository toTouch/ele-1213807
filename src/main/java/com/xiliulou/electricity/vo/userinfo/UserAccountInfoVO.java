package com.xiliulou.electricity.vo.userinfo;

import lombok.Builder;
import lombok.Data;

/**
 * @author HeYafeng
 * @description 账号管理用户信息
 * @date 2024/8/16 14:35:10
 */

@Builder
@Data
public class UserAccountInfoVO {
    
    private Long uid;
    
    /**
     * 审核状态 -1--未实名认证,0--等待审核中,1--审核被拒绝,2--审核通过
     */
    private Integer authStatus;
    
    private String userName;
    
    private String phone;
    
    private String idNumber;
}
