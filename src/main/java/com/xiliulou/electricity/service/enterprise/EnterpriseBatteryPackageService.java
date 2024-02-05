package com.xiliulou.electricity.service.enterprise;

import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseFreeDepositQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseMemberCardQuery;
import com.xiliulou.electricity.query.enterprise.EnterprisePackageOrderQuery;
import com.xiliulou.electricity.query.enterprise.EnterprisePurchaseOrderQuery;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletRequest;

/**
 * @author BaoYu
 * @date 2023-09-15 13:53
 */
public interface EnterpriseBatteryPackageService {

    public Triple<Boolean, String, Object> save(EnterpriseMemberCardQuery query);

    /**
     * 查询企业名下电池型号
     * @param query
     * @return
     */
    public Triple<Boolean, String, Object> queryBatterV(EnterpriseChannelUserQuery query);

    /**
     * 按电池型号，查询企业渠道用户套餐信息
     * @param query
     * @return
     */
    public Triple<Boolean, String, Object> queryPackagesByBatteryV(EnterpriseMemberCardQuery query);

    public Triple<Boolean, String, Object> queryByPackageId(EnterpriseMemberCardQuery query);
    
    public Triple<Boolean, String, Object> queryRiderDepositAndPackage(Long uid);
    
    @Deprecated
    public Triple<Boolean, String, Object>  queryUserBatteryDeposit(Long uid);
    
    public Triple<Boolean, String, Object> checkUserFreeBatteryDepositStatus(Long uid);
    
    public Triple<Boolean, String, Object> freeBatteryDeposit(EnterpriseFreeDepositQuery enterpriseFreeDepositQuery);

    /**
     * 企业站长为用户购买套餐
     * @param query
     * @return
     */
    public Triple<Boolean, String, Object> purchasePackageByEnterpriseUser(EnterprisePackageOrderQuery query);
    
    /**
     * 企业站长代付 套餐 + 押金 + 保险
     * @param query
     * @return
     */
    public Triple<Boolean, String, Object> purchasePackageWithDepositByEnterpriseUser(EnterprisePackageOrderQuery query);
    
    /**
     * 企业站长代付 免押 套餐+保险
     * @param query
     * @return
     */
    public Triple<Boolean, String, Object> purchasePackageWithFreeDeposit(EnterprisePackageOrderQuery query);

    /**
     * 查询骑手当前套餐详情信息
     * @param query
     * @return
     */
    public Triple<Boolean, String, Object> queryRiderDetails(EnterpriseMemberCardQuery query);
    
    /**
     * 查询骑手当前押金状态
     * @param query
     * @return
     */
    public Triple<Boolean, String, Object> queryDepositInfo(EnterpriseMemberCardQuery query);

    /**
     * 查询骑手消费详情信息
     * @return
     */
    public Triple<Boolean, String, Object> queryCostDetails(EnterprisePackageOrderQuery query);
    
    /**
     * 查询用户购买订单记录
     * @param query
     * @return
     */
    public Triple<Boolean, String, Object> queryPurchasedPackageOrders(EnterprisePurchaseOrderQuery query);
    
    /**
     * 根据企业ID查询加盟商信息
     * @param enterpriseId
     * @return
     */
    public Triple<Boolean, String, Object> selectFranchiseeByEnterpriseId(Long enterpriseId);
    
}
