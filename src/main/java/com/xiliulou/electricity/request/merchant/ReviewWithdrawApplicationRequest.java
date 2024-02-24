package com.xiliulou.electricity.request.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/24 14:59
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ReviewWithdrawApplicationRequest {
    
    private Long id;
    
    private Integer status;
    
    private String remark;
    
}
