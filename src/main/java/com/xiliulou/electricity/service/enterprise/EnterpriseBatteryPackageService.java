package com.xiliulou.electricity.service.enterprise;

import com.xiliulou.electricity.query.enterprise.EnterpriseMemberCardQuery;
import org.apache.commons.lang3.tuple.Triple;

/**
 * @author BaoYu
 * @date 2023-09-15 13:53
 */
public interface EnterpriseBatteryPackageService {

    public Triple<Boolean, String, Object> save(EnterpriseMemberCardQuery query);


}
