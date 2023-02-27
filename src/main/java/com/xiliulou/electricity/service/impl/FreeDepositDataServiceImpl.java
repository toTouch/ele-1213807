package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.FreeDepositData;
import com.xiliulou.electricity.entity.FreeDepositRechargeRecord;
import com.xiliulou.electricity.mapper.FreeDepositDataMapper;
import com.xiliulou.electricity.query.FreeDepositDataQuery;
import com.xiliulou.electricity.service.FreeDepositDataService;
import com.xiliulou.electricity.service.FreeDepositRechargeRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * (FreeDepositData)表服务实现类
 *
 * @author zzlong
 * @since 2023-02-20 15:46:34
 */
@Service("freeDepositDataService")
@Slf4j
public class FreeDepositDataServiceImpl implements FreeDepositDataService {
    @Autowired
    private FreeDepositDataMapper freeDepositDataMapper;

    @Autowired
    FreeDepositRechargeRecordService freeDepositRechargeRecordService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public FreeDepositData selectByIdFromDB(Long id) {
        return this.freeDepositDataMapper.selectById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public FreeDepositData selectByIdFromCache(Long id) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<FreeDepositData> selectByPage(int offset, int limit) {
        return this.freeDepositDataMapper.selectByPage(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param freeDepositData 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FreeDepositData insert(FreeDepositData freeDepositData) {
        this.freeDepositDataMapper.insertOne(freeDepositData);
        return freeDepositData;
    }

    /**
     * 修改数据
     *
     * @param freeDepositData 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(FreeDepositData freeDepositData) {
        return this.freeDepositDataMapper.update(freeDepositData);

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.freeDepositDataMapper.deleteById(id) > 0;
    }

    @Override
    public FreeDepositData selectByTenantId(Integer tenantId) {
        return this.freeDepositDataMapper.selectByTenantId(tenantId);
    }

    @Override
    public Triple<Boolean, String, Object> recharge(FreeDepositDataQuery freeDepositDataQuery) {
        FreeDepositData freeDepositData = this.selectByTenantId(freeDepositDataQuery.getTenantId());
        if (Objects.isNull(freeDepositData)) {
            FreeDepositData freeDepositDataInsert = new FreeDepositData();
            freeDepositDataInsert.setFreeDepositCapacity(freeDepositDataQuery.getFreeDepositCapacity());
            freeDepositDataInsert.setTenantId(TenantContextHolder.getTenantId());
            freeDepositDataInsert.setDelFlag(FreeDepositData.DEL_NORMAL);
            freeDepositDataInsert.setRechargeTime(System.currentTimeMillis());
            freeDepositDataInsert.setCreateTime(System.currentTimeMillis());
            freeDepositDataInsert.setUpdateTime(System.currentTimeMillis());
            this.freeDepositDataMapper.insertOne(freeDepositDataInsert);

            //保存充值记录
            freeDepositRechargeRecordService.insert(buildFreeDepositRechargeRecord(freeDepositDataInsert));
            return Triple.of(true, "", null);
        }

        FreeDepositData freeDepositDataUpdate = new FreeDepositData();
        freeDepositDataUpdate.setId(freeDepositData.getId());
        freeDepositDataUpdate.setFreeDepositCapacity(freeDepositData.getFreeDepositCapacity() + freeDepositDataQuery.getFreeDepositCapacity());
        freeDepositDataUpdate.setRechargeTime(System.currentTimeMillis());
        freeDepositDataUpdate.setUpdateTime(System.currentTimeMillis());
        this.freeDepositDataMapper.update(freeDepositDataUpdate);

        //保存充值记录
        freeDepositRechargeRecordService.insert(buildFreeDepositRechargeRecord(freeDepositDataUpdate));

        return Triple.of(true, "", null);
    }

    private FreeDepositRechargeRecord buildFreeDepositRechargeRecord(FreeDepositData freeDepositData) {
        FreeDepositRechargeRecord freeDepositRechargeRecord = new FreeDepositRechargeRecord();
        freeDepositRechargeRecord.setFreeRecognizeCapacity(freeDepositData.getFreeDepositCapacity());
        freeDepositRechargeRecord.setOperator(SecurityUtils.getUid());
        freeDepositRechargeRecord.setTenantId(freeDepositData.getTenantId());
        freeDepositRechargeRecord.setDelFlag(FreeDepositRechargeRecord.DEL_NORMAL);
        freeDepositRechargeRecord.setCreateTime(System.currentTimeMillis());
        freeDepositRechargeRecord.setUpdateTime(System.currentTimeMillis());

        return freeDepositRechargeRecord;
    }
}
