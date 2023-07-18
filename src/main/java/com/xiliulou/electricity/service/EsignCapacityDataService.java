package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.EsignCapacityData;
import com.xiliulou.electricity.query.EsignCapacityDataQuery;
import com.xiliulou.electricity.query.EsignCapacityRechargeRecordQuery;

import java.util.List;

/**
 * @author: Kenneth
 * @Date: 2023/7/11 20:10
 * @Description:
 */
public interface EsignCapacityDataService {

    EsignCapacityData queryCapacityDataByTenantId(Long tenantId);

    Integer addEsignCapacityData(EsignCapacityDataQuery esignCapacityDataQuery);

    Integer deductionCapacityByTenantId(Long tenantId);

    List<EsignCapacityRechargeRecordQuery> queryEsignRechargeRecords(EsignCapacityRechargeRecordQuery esignCapacityRechargeRecordQuery);

    Integer queryRecordsCount(EsignCapacityRechargeRecordQuery esignCapacityRechargeRecordQuery);

}
