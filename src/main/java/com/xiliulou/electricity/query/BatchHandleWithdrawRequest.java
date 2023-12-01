package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: 马小东
 * @Date: 2023/11/30 15:24
 * @Description: 批量提现审核请求
 */
@Data
public class BatchHandleWithdrawRequest {
    
    @NotNull(message = "id不能为空")
    private List<Integer> idList;
    
    @NotNull(message = "状态不能为空")
    private Integer status;
    
    private String msg;
    
    @NotEmpty(message = "密码不能为空")
    private String password;
}
