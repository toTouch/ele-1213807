package com.xiliulou.electricity.service;

import com.tencentcloudapi.faceid.v20180301.models.EidInfo;
import com.xiliulou.electricity.query.FaceidResultQuery;
import org.apache.commons.lang3.tuple.Triple;

public interface FaceidService {
    Triple<Boolean, String, Object> getEidToken();

    Triple<Boolean, String, Object> verifyEidResult(FaceidResultQuery faceidResultQuery);

}
