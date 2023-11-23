package com.xiliulou.electricity.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @description 小程序端 我的记录VO
 * @date 2023/11/17 17:58:56
 * @author HeYafeng
 */
@Builder
@Data
public class InvitationActivityRecordInfoVO {
    /**
     * 总的已赚金额
     */
    private BigDecimal totalMoney;
    
    /**
     * 总的邀请人数
     */
    private Integer totalInvitationCount;
    
    /**
     * 小程序端 我的记录集合
     */
    List<InvitationActivityRecordInfoListVO> invitationActivityRecordInfoList;
    
}
