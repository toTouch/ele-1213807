package com.xiliulou.electricity.service.enterprise;

import com.xiliulou.electricity.entity.UserBatteryMemberCard;
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
import org.springframework.transaction.annotation.Transactional;

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
    
    Triple<Boolean, String, Object> updateMerchantEnterprise(EnterpriseInfoQuery enterpriseInfoQuery);
    
    Triple<Boolean, String, Object> delete(Long id);
    
    Triple<Boolean, String, Object> modify(EnterpriseInfoQuery enterpriseInfoQuery);
    
    Triple<Boolean, String, Object> save(EnterpriseInfoQuery enterpriseInfoQuery);
    
    
    Triple<Boolean, String, Object> deleteMerchantEnterprise(Long id);
    
    Triple<Boolean, String, Object> rechargeForAdmin(EnterpriseCloudBeanRechargeQuery enterpriseCloudBeanRechargeQuery);
    
    Boolean checkUserType();
    
    EnterpriseInfo selectByUid(Long uid);
    
    EnterpriseInfo selectByName(String name);
    
    EnterpriseInfoVO selectEnterpriseInfoByUid(Long uid);
    
    EnterpriseInfo queryByIdFromCache(Long enterpriseId);
    
    UserCloudBeanDetailVO cloudBeanDetail();
    
    Triple<Boolean, String, Object> rechargeForUser(UserCloudBeanRechargeQuery userCloudBeanRechargeQuery, HttpServletRequest request);
    
    Triple<Boolean, String, Object> cloudBeanGeneralView();
    
    Triple<Boolean, String, Object> recycleCloudBean(Long uid, Long operateUid);
    
    void unbindUserData(UserInfo userInfo, EnterpriseChannelUser enterpriseChannelUser);
    
    Triple<Boolean, String, Object> recycleBatteryMembercard(UserInfo userInfo, EnterpriseInfo enterpriseInfo, UserBatteryMemberCard userBatteryMemberCard);
    
    Triple<Boolean, String, Object> recycleBatteryDeposit(UserInfo userInfo, EnterpriseInfo enterpriseInfo);
    
    Triple<Boolean, String, Object> recycleBatteryDepositV2(UserInfo userInfo, EnterpriseInfo enterpriseInfo, Long operateUid);
    
    List<EnterprisePurchasedPackageResultVO> queryPurchasedPackageCount(EnterprisePurchaseOrderQuery query);
    
    Integer updateAllRenewalStatus(EnterpriseInfoQuery enterpriseInfoQuery);
    
    EnterpriseInfoVO selectDetailByUid(Long uid);
    
    int addCloudBean(Long id, BigDecimal add);
    
    int subtractCloudBean(Long id, BigDecimal subtract, long updateTime);
    
    /**
     * 根据更换手机号
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    Integer updatePhoneByUid(Integer tenantId, Long uid, String newPhone);
    
    Triple<Boolean, String, Object> saveMerchantEnterprise(EnterpriseInfoQuery enterpriseInfoQuery);
    
    List<EnterpriseInfo> queryListByIdList(List<Long> enterpriseIdList);
    
    Triple<Boolean, String, Object> recycleBatteryMemberCardV2(UserInfo userInfo, EnterpriseInfo enterpriseInfo, UserBatteryMemberCard item, Long operateUid);
    
    List<EnterpriseInfo> queryList(Integer tenantId);
    
    void deleteCacheByEnterpriseId(Long enterpriseId);
    
    Triple<Boolean, String, Object> recycleCloudBeanForFreeDeposit(Long uid, Long operateUid);
}
