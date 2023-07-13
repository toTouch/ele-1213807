package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.EnableMemberCardRecord;
import com.xiliulou.electricity.entity.EsignCapacityData;

/**
 * @author: Kenneth
 * @Date: 2023/7/11 19:24
 * @Description:
 */
public interface EsignCapacityDataMapper extends BaseMapper<EnableMemberCardRecord> {

    int insertOne(EsignCapacityData esignCapacityData);

    int updateOne(EsignCapacityData esignCapacityData);

    EsignCapacityData selectByTenantId(Long tenantId);
    Integer deductionCapacity(EsignCapacityData esignCapacityData);

}
