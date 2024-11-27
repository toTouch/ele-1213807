package com.xiliulou.electricity.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @ClassName: MutualExchangeAddConfigRequest
 * @description:
 * @author: renhang
 * @create: 2024-11-27 15:33
 */
@Data
public class MutualExchangeAddConfigRequest {
    
    @NotBlank(message = "组合名称不能为空!")
    private String combinedName;
    
    /**
     * 组合加盟商
     */
    private List<String> combinedFranchisee;
    
    
    private Integer status;
}
