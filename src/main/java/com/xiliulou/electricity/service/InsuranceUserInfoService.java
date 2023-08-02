package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.InsuranceUserInfoQuery;
import com.xiliulou.electricity.vo.InsuranceUserInfoVo;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * 换电柜保险用户绑定(InsuranceUserInfo)表服务接口
 *
 * @author makejava
 * @since 2022-11-02 13:37:11
 */
public interface InsuranceUserInfoService {


    List<InsuranceUserInfo> selectByInsuranceId(Integer id, Integer tenantId);

    InsuranceUserInfo queryByUid(Long uid, Integer tenantId);

    R updateUserBatteryInsuranceStatus(Long uid, Integer insuranceStatus,Integer type);

    InsuranceUserInfo queryByUidFromCache(Long uid);

    InsuranceUserInfo selectByUidAndTypeFromDB(Long uid,Integer type);

    InsuranceUserInfo selectByUidAndTypeFromCache(Long uid,Integer type);

    Integer insert(InsuranceUserInfo insuranceUserInfo);

    Integer update(InsuranceUserInfo insuranceUserInfo);

    int updateInsuranceUserInfoById(InsuranceUserInfo insuranceUserInfo);

    InsuranceUserInfoVo queryByUidAndTenantId(Long uid, Integer tenantId);

    R queryUserInsurance();

    R queryUserInsurance(Long uid,Integer type);

    R queryInsuranceByStatus(Integer status, Long offset, Long size);

    int deleteById(InsuranceUserInfo insuranceUserInfo);

    int deleteByUidAndType(Long uid,Integer type);

    R insertUserBatteryInsurance(InsuranceUserInfoQuery query);

    R editUserInsuranceInfo(InsuranceUserInfoQuery query);

    R renewalUserBatteryInsurance(InsuranceUserInfoQuery query);

    Boolean verifyUserIsNeedBuyInsurance(UserInfo userInfo, Integer type, String simpleBatteryType, Long carModelId);

    InsuranceUserInfoVo selectUserInsuranceDetailByUidAndType(Long uid, Integer type);
}
