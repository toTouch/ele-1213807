package com.xiliulou.electricity.service.enterprise;

import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.RentBatteryOrder;
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
    
    /**
     * 购买套餐时，记录骑手消费详情信息
     */
    void asyncSaveUserCostRecordForPurchasePackage(ElectricityMemberCardOrder electricityMemberCardOrder, EleDepositOrder eleDepositOrder, InsuranceOrder insuranceOrder);
    
    /**
     * 企业套餐冻结及启用时，记录骑手消费详情信息
     * @param uid
     * @param orderId
     * @param costType
     * @param createTime
     */
    void asyncSaveUserCostRecordForBattery(Long uid, String orderId, Integer costType, Long createTime);
    
    /**
     * 企业套餐回收云豆，即退押时，记录骑手消费详情信息
     * @param uid
     * @param costType
     * @param eleRefundOrder
     */
    void asyncSaveUserCostRecordForRefundDeposit(Long uid, Integer costType, EleRefundOrder eleRefundOrder);
    
    /**
     * 企业套餐租，退电池时，记录骑手消费详情信息
     * @param costType
     * @param rentBatteryOrder
     */
    void asyncSaveUserCostRecordForRentalAndReturnBattery(Integer costType, RentBatteryOrder rentBatteryOrder);
    
}
