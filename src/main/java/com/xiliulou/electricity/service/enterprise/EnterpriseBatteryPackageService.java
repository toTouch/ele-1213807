package com.xiliulou.electricity.service.enterprise;

import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseMemberCardQuery;
import com.xiliulou.electricity.query.enterprise.EnterprisePackageOrderQuery;
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

    /**
     * 企业站长为用户购买套餐
     * @param query
     * @return
     */
    public Triple<Boolean, String, Object> purchasePackageByEnterpriseUser(EnterprisePackageOrderQuery query, HttpServletRequest request);

    /**
     * 查询骑手当前套餐详情信息
     * @param query
     * @return
     */
    public Triple<Boolean, String, Object> queryRiderDetails(EnterpriseMemberCardQuery query);

    /**
     * 查询骑手消费详情信息
     * @return
     */
    public Triple<Boolean, String, Object> queryCostDetails(EnterprisePackageOrderQuery query);



}
