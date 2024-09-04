package com.xiliulou.electricity.query.profitsharing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/8/23 10:47
 * @desc
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProfitSharingOrderDetailQueryModel {
    private Long size;
    
    private Long offset;
    
    private Long startTime;
    
    private Long endTime;
    
    /**
     * 分账方类型 0:默认，1：加盟商
     */
    private Integer outAccountType;
    
    private List<Long> franchiseeIdList;
    
    /**
     * 分账接收方
     */
    private String profitSharingReceiveName;
    
    /**
     * 分账状态：0：已受理、1：处理中、2：分账完成，3：分账失败
     */
    private Integer status;
    
    /**
     * 业务类型：0：换电-套餐购买、1：换电-保险购买、2：换电-滞纳金缴纳、3：换电-押金缴纳, 98: 解冻，99：系统级别
     */
    private Integer businessType;
    
    /**
     * 第三方支付订单号
     */
    private String thirdTradeOrderNo;
    
    private Integer tenantId;
}
