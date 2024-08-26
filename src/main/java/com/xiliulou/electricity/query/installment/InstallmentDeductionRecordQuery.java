package com.xiliulou.electricity.query.installment;

import lombok.Data;

import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/27 17:40
 */
@Data
public class InstallmentDeductionRecordQuery {
    
    private Integer offset;
    
    private Integer size;
    
    /**
     * 请求签约用户uid
     */
    private Long uid;
    
    /**
     * 请求签约号，签约订单编号
     */
    private String externalAgreementNo;
    
    /**
     * 代扣订单号
     */
    private String payNo;
    /**
     * 签约状态
     */
    private Integer status;
    
    private Integer tenantId;
    
    private Long franchiseeId;
    
    private List<Long> franchiseeIds;
    
    private List<Long> storeIds;
}
