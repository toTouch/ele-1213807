package com.xiliulou.electricity.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

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
    
    private Long id;
    
    private String combinedName;
    
    /**
     * 组合加盟商
     */
    private List<Long> combinedFranchisee;
    
    
    private Integer status;
}
