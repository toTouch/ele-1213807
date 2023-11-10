package com.xiliulou.electricity.dto;

import com.xiliulou.electricity.enums.enterprise.EnterpriseUserCostRecordTypeEnum;
import com.xiliulou.electricity.enums.enterprise.UserCostTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author BaoYu
 * @description:
 * @date 2023/10/19 11:50
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnterpriseUserCostRecordDTO {
    
    /**
     * 用户UID
     */
    private Long uid;
    
    /**
     * 企业ID
     */
    private Long enterpriseId;
    
    /**
     * 消费类型： 1-购买套餐, 2-租电池, 3-还电池, 4-冻结套餐, 5-启用套餐, 6-退押金
     * @see UserCostTypeEnum
     */
    private Integer costType;
    
    /**
     * 消费订单编号
     */
    private String orderId;
    
    /**
     * 类型(1-企业渠道电池订单, 2-企业渠道租车订单)
     * @see EnterpriseUserCostRecordTypeEnum
     */
    private Integer type;
    
    /**
     * 套餐ID
     */
    private Long packageId;
    
    /**
     * 套餐名称
     */
    private String packageName;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 租户ID
     */
    private Long tenantId;
    
    /**
     * 链路ID
     */
    private String traceId;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 修改时间
     */
    private Long updateTime;
    
}
