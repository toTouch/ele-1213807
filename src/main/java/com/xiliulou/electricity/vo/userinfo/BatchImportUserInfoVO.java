package com.xiliulou.electricity.vo.userinfo;

import lombok.Data;

import java.util.Set;

/**
 * @author HeYafeng
 * @description 用户分组-批量导入用户
 * @date 2024/4/9 15:42:47
 */
@Data
public class BatchImportUserInfoVO {
    
    /**
     * 不存在的分组
     */
    private Set<Long> notExistUserGroups;
    
    /**
     * 用户不存在的手机号
     */
    private Set<String> notExistPhones;
    
    /**
     * 未绑定加盟商的手机号
     */
    private Set<String> notBoundFranchiseePhones;
    
    /**
     * 加盟商不一致的手机号
     */
    private Set<String> notSameFranchiseePhones;
    
    private String sessionId;
    
    private Boolean isImported;
}
