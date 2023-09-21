package com.xiliulou.electricity.service.enterprise;

import com.xiliulou.electricity.query.enterprise.EnterpriseChannelUserQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseMemberCardQuery;
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
     * 生成企业渠道用户数基础数据
     * @param query
     * @return
     */
    public Triple<Boolean, String, Object> generateChannelUser(EnterpriseChannelUserQuery query);

    /**
     *
     * @param query
     * @return
     */
    public Triple<Boolean, String, Object> updateUserAfterQRScan(EnterpriseChannelUserQuery query);

    public Triple<Boolean, String, Object> checkUserExist(Long id, Long uid);
}
