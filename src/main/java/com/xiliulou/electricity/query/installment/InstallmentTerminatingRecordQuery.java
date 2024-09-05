package com.xiliulou.electricity.query.installment;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/28 14:44
 */
@Data
@Builder
public class InstallmentTerminatingRecordQuery {
    
    private Integer offset;
    
    private Integer size;
    
    /**
     * 请求签约号，唯一
     */
    private String externalAgreementNo;
    
    /**
     * 请求签约用户uid
     */
    private Long uid;
    
    /**
     * 审核状态
     */
    private Integer status;
    
    private List<Integer> statuses;
    
    private Integer tenantId;
    
    private Long franchiseeId;
    
    private List<Long> franchiseeIds;
    
    private Long createTime;
}
