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

    public Triple<Boolean, String, Object> save(EnterpriseChannelUserQuery query);
}
