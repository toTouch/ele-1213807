package com.xiliulou.electricity.vo.merchant;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author HeYafeng
 * @description 修改邀请人
 * @date 2024/3/27 09:10:47
 */
@Builder
@Data
public class MerchantModifyInviterVO {
    
    /**
     * 用户uid
     */
    private Long uid;
    
    /**
     * 邀请人名称
     */
    private String inviterName;
    
    /**
     * 邀请人来源：1-商户邀请 2-用户邀请
     */
    private Integer inviterSource;
    
    private Long franchiseeId;
    
    private String franchiseeName;
    
    List<MerchantVO> merchantList;
}
