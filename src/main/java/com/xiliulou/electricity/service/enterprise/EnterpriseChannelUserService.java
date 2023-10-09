package com.xiliulou.electricity.service.enterprise;

import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;
import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseMemberCardQuery;
import com.xiliulou.electricity.vo.enterprise.EnterpriseChannelUserVO;
import org.apache.commons.lang3.tuple.Triple;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/18 13:58
 */
public interface EnterpriseChannelUserService {

    /**
     * 企业渠道新增关联用户
     * @param query
     * @return
     */
    public Triple<Boolean, String, Object> save(EnterpriseChannelUserQuery query);

    /**
     * 根据手机号码，及运营商信息查询用户
     * @param enterpriseChannelUserQuery
     * @return
     */
    public Triple<Boolean, String, Object> queryUser(EnterpriseChannelUserQuery enterpriseChannelUserQuery);

    /**
     * 生成企业渠道用户基础数据
     * @param query
     * @return
     */
    public Triple<Boolean, String, Object> generateChannelUser(EnterpriseChannelUserQuery query);

    /**
     * 扫码成功后，将用户更新至企业渠道用户表中
     * @param query
     * @return
     */
    public Triple<Boolean, String, Object> updateUserAfterQRScan(EnterpriseChannelUserQuery query);
    
    /**
     * 检测指定用户是否与企业关联
     * @param id
     * @param uid
     * @return
     */
    public Triple<Boolean, String, Object> checkUserExist(Long id, Long uid);
    
    /**
     * 根据企业ID和用户UID查询当前企业渠道用户信息
     * @param enterpriseId
     * @param uid
     * @return
     */
    public EnterpriseChannelUserVO selectUserByEnterpriseIdAndUid(Long enterpriseId, Long uid);
    
    EnterpriseChannelUser selectByUid(Long uid);
    
    EnterpriseChannelUserVO queryEnterpriseChannelUser(Long uid);
    
    EnterpriseChannelUserVO queryUserRelatedEnterprise(Long uid);
    
    Integer updateChannelUser(EnterpriseChannelUserQuery enterpriseChannelUserQuery);
    
    Boolean checkUserRenewalStatus(EnterpriseChannelUserQuery enterpriseChannelUserQuery);
    
}
