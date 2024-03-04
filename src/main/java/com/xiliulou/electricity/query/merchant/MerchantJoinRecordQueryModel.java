package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 商户参与记录
 * @date 2024/3/2 18:53:34
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantJoinRecordQueryModel {
    
    private Long joinUid;
    
    /**
     * 参与状态 1-已参与，2-邀请成功，3-已过期，4-已失效
     */
    private Integer status;
    
    private Long updateTime;
}
