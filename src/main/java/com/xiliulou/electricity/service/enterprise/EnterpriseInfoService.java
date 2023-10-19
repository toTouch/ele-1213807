package com.xiliulou.electricity.service.enterprise;

import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;
import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;
import com.xiliulou.electricity.query.enterprise.EnterpriseCloudBeanRechargeQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseInfoQuery;
import com.xiliulou.electricity.query.enterprise.EnterprisePurchaseOrderQuery;
import com.xiliulou.electricity.query.enterprise.UserCloudBeanRechargeQuery;
import com.xiliulou.electricity.vo.enterprise.EnterpriseInfoVO;
import com.xiliulou.electricity.vo.enterprise.EnterprisePurchasedPackageResultVO;
import com.xiliulou.electricity.vo.enterprise.UserCloudBeanDetailVO;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

/**
 * 企业用户信息表(EnterpriseInfo)表服务接口
 *
 * @author zzlong
 * @since 2023-09-14 10:15:08
 */
public interface EnterpriseInfoService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    EnterpriseInfo queryByIdFromDB(Long id);

    /**
     * 修改数据
     *
     * @param enterpriseInfo 实例对象
     * @return 实例对象
     */
    Integer update(EnterpriseInfo enterpriseInfo);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Integer deleteById(Long id);

    List<EnterpriseInfoVO> selectByPage(EnterpriseInfoQuery query);

    Integer selectByPageCount(EnterpriseInfoQuery query);

    Triple<Boolean, String, Object> delete(Long id);

    Triple<Boolean, String, Object> modify(EnterpriseInfoQuery enterpriseInfoQuery);

    Triple<Boolean, String, Object> save(EnterpriseInfoQuery enterpriseInfoQuery);
    
    

    Triple<Boolean, String, Object> rechargeForAdmin(EnterpriseCloudBeanRechargeQuery enterpriseCloudBeanRechargeQuery);

    Boolean checkUserType();

    EnterpriseInfo selectByUid(Long uid);
    
    EnterpriseInfo selectByName(String name);
    
    EnterpriseInfoVO selectEnterpriseInfoByUid(Long uid);

    EnterpriseInfo queryByIdFromCache(Long enterpriseId);

    UserCloudBeanDetailVO cloudBeanDetail();

    Triple<Boolean, String, Object> rechargeForUser(UserCloudBeanRechargeQuery userCloudBeanRechargeQuery, HttpServletRequest request);
    
    Triple<Boolean, String, Object> cloudBeanGeneralView();
    
    Triple<Boolean, String, Object> recycleCloudBean(Long uid);
    
    void unbindUserData(UserInfo userInfo, EnterpriseChannelUser enterpriseChannelUser);
    
    Triple<Boolean, String, Object> recycleBatteryMembercard(UserInfo userInfo, EnterpriseInfo enterpriseInfo);
    
    Triple<Boolean, String, Object> recycleBatteryDeposit(UserInfo userInfo, EnterpriseInfo enterpriseInfo);
    
    List<EnterprisePurchasedPackageResultVO> queryPurchasedPackageCount(EnterprisePurchaseOrderQuery query);
    
    Integer updateAllRenewalStatus(EnterpriseInfoQuery enterpriseInfoQuery);
    
    EnterpriseInfoVO selectDetailByUid(Long uid);
    
    Triple<Boolean, String, Object> refund(String orderId,HttpServletRequest request);
    
    int addCloudBean(Long id, BigDecimal add);
}
