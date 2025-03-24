package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.entity.UserInfo;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author : eclair
 * @date : 2024/2/18 19:19
 */
@Data
public class BatchUnbindGroupVO {
    
    private Set<String> notExistPhones;
    
    private List<String> notExistsFranchisee;
    
    private List<String> notExistsGroups;
    
    private String sessionId;
    
    private Boolean isSend;
}
