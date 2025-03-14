package com.xiliulou.electricity.request.user;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author SJP
 * @date 2025-02-21 15:33
 **/
@Data
public class UserRequest {
    @NotNull(message = "分页参数不能为空")
    private Long size;
    
    @NotNull(message = "分页参数不能为空")
    private Long offset;
    
    private Long uid;
    
    private String name;
    
    // 租户、加盟商、门店的id都会收集到这个集合中用于查询user
    private List<Long> allDataIds;
    
    // 为了分页需要把租户管理员的uid传回去一起查询
    private List<Long> tenantUserUids;
}
