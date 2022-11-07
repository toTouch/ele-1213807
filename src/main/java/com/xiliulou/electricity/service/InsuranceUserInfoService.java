package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.entity.UserInfo;

import java.util.List;

/**
 * 换电柜保险用户绑定(InsuranceUserInfo)表服务接口
 *
 * @author makejava
 * @since 2022-11-02 13:37:11
 */
public interface InsuranceUserInfoService {


    List<InsuranceUserInfo> selectByInsuranceId(Integer id, Integer tenantId);

    InsuranceUserInfo queryByUid(Long uid,Integer tenantId);

    R updateInsuranceStatus(Long uid,Integer insuranceStatus);

    InsuranceUserInfo queryByUidFromCache(Long uid);

    Integer insert(InsuranceUserInfo insuranceUserInfo);

    Integer update(InsuranceUserInfo insuranceUserInfo);

}
