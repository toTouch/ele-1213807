package com.xiliulou.electricity.service;

import com.xiliulou.electricity.query.AlipayUserCertifyInfoQuery;
import com.xiliulou.electricity.query.FaceidResultQuery;
import com.xiliulou.electricity.query.UserCertifyInfoQuery;
import org.apache.commons.lang3.tuple.Triple;

public interface FaceidService {
    Triple<Boolean, String, Object> getEidToken();

    Triple<Boolean, String, Object> verifyEidResult(FaceidResultQuery faceidResultQuery);
    
    Triple<Boolean, String, Object> queryAliPayCertifyInfo(AlipayUserCertifyInfoQuery query);
    
    Triple<Boolean, String, Object> queryAliPayUserCertifyResult(AlipayUserCertifyInfoQuery query);
    
    Triple<Boolean, String, Object> saveUserCertifyInfo(UserCertifyInfoQuery userCertifyInfoQuery);
}
