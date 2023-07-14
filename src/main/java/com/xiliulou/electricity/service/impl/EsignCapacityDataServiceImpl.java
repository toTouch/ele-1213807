package com.xiliulou.electricity.service.impl;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.EleEsignConstant;
import com.xiliulou.electricity.entity.EsignCapacityData;
import com.xiliulou.electricity.entity.EsignCapacityRechargeRecord;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.EsignCapacityDataMapper;
import com.xiliulou.electricity.mapper.EsignCapacityRechargeRecordMapper;
import com.xiliulou.electricity.query.EsignCapacityDataQuery;
import com.xiliulou.electricity.query.EsignCapacityRechargeRecordQuery;
import com.xiliulou.electricity.service.EsignCapacityDataService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author: Kenneth
 * @Date: 2023/7/11 20:34
 * @Description:
 */

@Service
@Slf4j
public class EsignCapacityDataServiceImpl implements EsignCapacityDataService {

    @Autowired
    private EsignCapacityDataMapper esignCapacityDataMapper;

    @Autowired
    private EsignCapacityRechargeRecordMapper esignCapacityRechargeRecordMapper;

    @Autowired
    private UserService userService;

    @Slave
    @Override
    public EsignCapacityData queryCapacityDataByTenantId(Long tenantId) {
        return esignCapacityDataMapper.selectByTenantId(tenantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer addEsignCapacityData(EsignCapacityDataQuery esignCapacityDataQuery) {

        EsignCapacityData capacityData = queryCapacityDataByTenantId(esignCapacityDataQuery.getTenantId());

        if(Objects.isNull(capacityData)){
            log.info("add esign capacity data: {}", esignCapacityDataQuery);
            EsignCapacityData esignCapacityData = new EsignCapacityData();
            esignCapacityData.setTenantId(TenantContextHolder.getTenantId().longValue());
            esignCapacityData.setEsignCapacity(esignCapacityDataQuery.getEsignCapacity());
            esignCapacityData.setDelFlag(EleEsignConstant.DEL_NO);
            esignCapacityData.setRechargeTime(System.currentTimeMillis());
            esignCapacityData.setCreateTime(System.currentTimeMillis());
            esignCapacityData.setUpdateTime(System.currentTimeMillis());
            esignCapacityDataMapper.insertOne(esignCapacityData);
        }else{
            log.info("update esign capacity data: {}", esignCapacityDataQuery);
            int currentCapacity = capacityData.getEsignCapacity();
            EsignCapacityData capacityDataModel = new EsignCapacityData();
            capacityDataModel.setId(capacityData.getId());
            capacityDataModel.setTenantId(capacityData.getTenantId());
            capacityDataModel.setEsignCapacity(currentCapacity + esignCapacityDataQuery.getEsignCapacity());
            capacityDataModel.setUpdateTime(System.currentTimeMillis());
            esignCapacityDataMapper.updateOne(capacityDataModel);
        }

        EsignCapacityRechargeRecord esignCapacityRechargeRecord = new EsignCapacityRechargeRecord();
        esignCapacityRechargeRecord.setEsignCapacity(esignCapacityDataQuery.getEsignCapacity());
        esignCapacityRechargeRecord.setTenantId(esignCapacityDataQuery.getTenantId());
        esignCapacityRechargeRecord.setOperator(SecurityUtils.getUid());
        esignCapacityRechargeRecord.setDelFlag(EleEsignConstant.DEL_NO);
        esignCapacityRechargeRecord.setCreateTime(System.currentTimeMillis());
        esignCapacityRechargeRecord.setUpdateTime(System.currentTimeMillis());

        return esignCapacityRechargeRecordMapper.insertOne(esignCapacityRechargeRecord);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deductionCapacityByTenantId(Long tenantId) {
        EsignCapacityData capacityData = new EsignCapacityData();
        capacityData.setTenantId(tenantId);
        capacityData.setUpdateTime(System.currentTimeMillis());

        return esignCapacityDataMapper.deductionCapacity(capacityData);
    }

    @Slave
    @Override
    public List<EsignCapacityRechargeRecordQuery> queryEsignRechargeRecords(EsignCapacityRechargeRecordQuery esignCapacityRechargeRecordQuery) {
        List<EsignCapacityRechargeRecord> rechargeRecords = esignCapacityRechargeRecordMapper.selectByPage(esignCapacityRechargeRecordQuery);

        List<EsignCapacityRechargeRecordQuery> rechargeRecordQueries = new ArrayList<>();
        if (CollectionUtils.isEmpty(rechargeRecords)) {
            return Collections.EMPTY_LIST;
        }

        for(EsignCapacityRechargeRecord esignCapacityRechargeRecord : rechargeRecords){
            EsignCapacityRechargeRecordQuery rechargeRecordQuery = new EsignCapacityRechargeRecordQuery();
            BeanUtils.copyProperties(esignCapacityRechargeRecord, rechargeRecordQuery);
            if (Objects.nonNull(esignCapacityRechargeRecord.getOperator())) {
                User user = userService.queryByUidFromCache(esignCapacityRechargeRecord.getOperator());
                rechargeRecordQuery.setOperatorName(user.getName());
            }
            rechargeRecordQueries.add(rechargeRecordQuery);
        }

        return rechargeRecordQueries;
    }
    @Slave
    @Override
    public Integer queryRecordsCount(EsignCapacityRechargeRecordQuery esignCapacityRechargeRecordQuery) {
        return esignCapacityRechargeRecordMapper.selectByPageCount(esignCapacityRechargeRecordQuery);
    }
}
