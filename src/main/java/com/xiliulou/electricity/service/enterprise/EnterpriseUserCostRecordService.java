package com.xiliulou.electricity.service.enterprise;

import com.xiliulou.electricity.dto.EnterpriseUserCostRecordDTO;
import com.xiliulou.electricity.query.enterprise.EnterpriseUserCostRecordQuery;
import com.xiliulou.electricity.vo.enterprise.EnterprisePackageOrderVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseUserCostDetailsVO;

import java.util.List;

/**
 * @author BaoYu
 * @description:
 * @date 2023/10/19 11:01
 */
public interface EnterpriseUserCostRecordService {
    
    /**
     * 查询骑手消费详情信息
     * @param enterpriseUserCostRecordQuery
     * @return
     */
    List<EnterpriseUserCostDetailsVO> queryUserCostRecordList(EnterpriseUserCostRecordQuery enterpriseUserCostRecordQuery);
    
    
    EnterpriseUserCostRecordDTO buildUserCostRecordForPurchasePackage(Long uid, String orderId, Long enterpriseId, Long packageId, Integer costType);
    
    
    void asyncSaveUserCostRecordForBattery(Long uid, String orderId, Integer costType, Long createTime);
    
    //void asyncSaveUserCostRecordForFreezeBattery(Long uid, String orderId, Integer costType);
    
}
