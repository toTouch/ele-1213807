package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.dto.FranchiseeInsuranceCarModelAndBatteryTypeDTO;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.FranchiseeMoveInfo;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.query.FranchiseeInsuranceIdsRequest;
import com.xiliulou.electricity.query.FranchiseeInsuranceAddAndUpdate;
import com.xiliulou.electricity.query.FranchiseeInsuranceQuery;
import com.xiliulou.electricity.vo.FranchiseeInsuranceVo;
import com.xiliulou.electricity.vo.UserInsuranceInfoVO;
import com.xiliulou.electricity.vo.insurance.FranchiseeInsuranceOrderIdsVo;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Collection;
import java.util.List;

/**
 * 换电柜保险(FranchiseeInsurance)表服务接口
 *
 * @author makejava
 * @since 2022-11-02 13:37:11
 */
public interface FranchiseeInsuranceService {


    R add(FranchiseeInsuranceAddAndUpdate franchiseeInsuranceAddAndUpdate);

    R update(FranchiseeInsuranceAddAndUpdate franchiseeInsuranceAddAndUpdate);

    R enableOrDisable(Long id,Integer status);

    R delete(Integer id);

    R queryList(Long offset, Long size, Integer status, Integer type,Integer tenantId,Long franchiseeId);

    R queryCount(Integer status, Integer type, Integer tenantId, Long franchiseeId);

    FranchiseeInsurance queryByIdFromCache(Integer id);

    FranchiseeInsurance queryByFranchiseeId(Long franchiseeId,String batteryType,Integer tenantId);
    FranchiseeInsurance queryByFranchiseeIdAndSimpleBatteryType(Long franchiseeId,String batteryType,Integer tenantId);

    R queryCanAddInsuranceBatteryType(Long franchiseeId);

    FranchiseeInsurance selectById(Integer insuranceId);

    List<FranchiseeInsurance> selectByFranchiseeId(Long toFranchiseeId, Integer tenantId);

    void moveInsurance(FranchiseeMoveInfo franchiseeMoveInfo, Franchisee newFranchisee);

    R selectInsuranceListByCondition( Integer status, Integer type, Integer tenantId, Long franchiseeId, String batterType);

    FranchiseeInsurance selectByFranchiseeIdAndType(Long franchiseeId, int insuranceTypeBattery, String batteryV);

    List<FranchiseeInsuranceVo> selectByPage(FranchiseeInsuranceQuery query);

    Integer selectPageCount(FranchiseeInsuranceQuery query);

    FranchiseeInsurance selectInsuranceByType(FranchiseeInsuranceQuery query);

    Triple<Boolean, String, Object> selectInsuranceByUid(Long uid, Integer type);
    
    List<FranchiseeInsuranceCarModelAndBatteryTypeDTO> selectListCarModelAndBatteryTypeById(Collection<Long> collect);
    
    List<FranchiseeInsuranceOrderIdsVo> queryInsuranceIds(FranchiseeInsuranceIdsRequest franchiseeInsuranceIdsRequest);

    FranchiseeInsurance querySameInsuranceType(Integer tenantId, Long franchiseeId, Integer type, String batteryType, Long carModelId);

    UserInsuranceInfoVO selectInsuranceByTypeV2(FranchiseeInsuranceQuery query);
}
