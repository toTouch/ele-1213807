package com.xiliulou.electricity.vo.installment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/12/5 10:27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentDeductionPlanAssemblyVO {
    /**
     * 请求签约号，唯一
     */
    private String externalAgreementNo;
    
    /**
     * 分期期次
     */
    private Integer issue;
    
    /**
     * 分期套餐id
     */
    private Long packageId;
    
    /**
     * 分期套餐类型，0-换电，1-租车，2-车电一体
     */
    private Integer packageType;
    
    /**
     * 租期，单位天
     */
    private Integer rentTime;
    
    /**
     * 应还款时间
     */
    private Long deductTime;
    
    private Integer tenantId;
    
    private Long franchiseeId;
    
    private List<InstallmentDeductionPlanEachVO> installmentDeductionPlanEachVOs;
}
