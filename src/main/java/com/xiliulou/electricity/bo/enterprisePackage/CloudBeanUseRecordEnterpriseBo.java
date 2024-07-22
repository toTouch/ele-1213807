package com.xiliulou.electricity.bo.enterprisePackage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/7/11 8:47
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CloudBeanUseRecordEnterpriseBo {
    /**
     * 企业id
     */
    private Long enterpriseId;
    
    /**
     * 企业名称
     */
    private String enterpriseName;
}
