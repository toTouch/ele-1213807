package com.xiliulou.electricity.service.enterprise;

import com.xiliulou.electricity.dto.EnterpriseUserCostRecordDTO;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.query.enterprise.EnterpriseMemberCardQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseUserCostRecordQuery;
import com.xiliulou.electricity.vo.enterprise.EnterpriseUserCostDetailsVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * @author BaoYu
 * @description:
 * @date 2023/10/19 11:01
 */
public interface EnterpriseUserCostRecordService {
    
    /**
     * 查询骑手套餐购买详情信息
     * @param query
     * @return
     */
    Triple<Boolean, String, Object> queryRiderDetails(EnterpriseMemberCardQuery query);
    
    /**
     * 查询骑手消费详情信息
     * @param enterpriseUserCostRecordQuery
     * @return
     */
    List<EnterpriseUserCostDetailsVO> queryUserCostRecordList(EnterpriseUserCostRecordQuery enterpriseUserCostRecordQuery);
    
    EnterpriseUserCostRecordDTO buildUserCostRecordForPurchasePackage(Long uid, String orderId, Long enterpriseId, Long packageId, Integer costType);
    
    void asyncSaveUserCostRecordForBattery(Long uid, String orderId, Integer costType, Long createTime);
    
    //void asyncSaveUserCostRecordForFreezeBattery(Long uid, String orderId, Integer costType);
    
    void asyncSaveUserCostRecordForRefundDeposit(Long uid, Integer costType, EleRefundOrder eleRefundOrder);
    
}
