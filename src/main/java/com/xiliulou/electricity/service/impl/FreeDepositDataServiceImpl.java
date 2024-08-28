package com.xiliulou.electricity.service.impl;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.entity.FreeDepositData;
import com.xiliulou.electricity.entity.FreeDepositRechargeRecord;
import com.xiliulou.electricity.enums.message.RechargeAlarm;
import com.xiliulou.electricity.enums.message.SiteMessageType;
import com.xiliulou.electricity.event.SiteMessageEvent;
import com.xiliulou.electricity.event.publish.SiteMessagePublish;
import com.xiliulou.electricity.mapper.FreeDepositDataMapper;
import com.xiliulou.electricity.query.FreeDepositDataQuery;
import com.xiliulou.electricity.query.FreeDepositFyRequest;
import com.xiliulou.electricity.service.FreeDepositDataService;
import com.xiliulou.electricity.service.FreeDepositRechargeRecordService;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    
    @Autowired
    private SiteMessagePublish siteMessagePublish;
    
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
    
    @Slave
    @Override
    public FreeDepositData selectByTenantId(Integer tenantId) {
        //发送站内信
        FreeDepositData freeDepositData = this.freeDepositDataMapper.selectByTenantId(tenantId);
        
        if (Objects.isNull(freeDepositData)) {
            return null;
        }
        final Integer total =
                ObjectUtils.defaultIfNull(freeDepositData.getFreeDepositCapacity(), CommonConstant.ZERO) + ObjectUtils.defaultIfNull(freeDepositData.getFyFreeDepositCapacity(),
                        CommonConstant.ZERO);
        Optional.ofNullable(tenantId).ifPresent(id -> siteMessagePublish.publish(
                SiteMessageEvent.builder(this).code(SiteMessageType.INSUFFICIENT_RECHARGE_BALANCE).notifyTime(System.currentTimeMillis()).tenantId(id.longValue())
                        .addContext("type", RechargeAlarm.SESAME_CREDIT).addContext("count", total).build()));
        
        return freeDepositData;
    }
    
    @Override
    public Integer deductionFreeDepositCapacity(Integer tenantId, Integer count) {
        return this.freeDepositDataMapper.deductionFreeDepositCapacity(tenantId, count);
    }
    
    @Override
    public Integer deductionFreeDepositCapacity(Integer tenantId, Integer count , Integer channel) {
        if (channel.equals(FreeDepositData.FREE_TYPE_PXZ)){
            return this.freeDepositDataMapper.deductionFreeDepositCapacity(tenantId, count);
        }else {
            return this.freeDepositDataMapper.deductionFyFreeDepositCapacity(tenantId, count);
        }
    }
    
    @Override
    public Integer deductionFyFreeDepositCapacity(Integer tenantId, Integer count) {
        return this.freeDepositDataMapper.deductionFyFreeDepositCapacity(tenantId, count);
    }
    
    @Override
    public Pair<Boolean, String> rechargeFY(FreeDepositFyRequest params) {
        
        if (Objects.isNull(params.getFreeDepositCapacity()) && Objects.isNull(params.getByStagesCapacity())) {
            return Pair.of(false, "充值次数不能为空");
        }
        
        FreeDepositData freeDepositData = selectByTenantId(params.getTenantId());
        boolean hasData = true;
        if (Objects.isNull(freeDepositData)) {
            hasData = false;
            freeDepositData = new FreeDepositData();
            freeDepositData.setCreateTime(System.currentTimeMillis());
            freeDepositData.setFreeDepositCapacity(CommonConstant.ZERO);
            freeDepositData.setTenantId(params.getTenantId());
        }
        
        freeDepositData.setFyFreeDepositCapacity(
                ObjectUtils.defaultIfNull(freeDepositData.getFyFreeDepositCapacity(), CommonConstant.ZERO) + ObjectUtils.defaultIfNull(params.getFreeDepositCapacity(),
                        CommonConstant.ZERO));
        freeDepositData.setRechargeTime(System.currentTimeMillis());
        freeDepositData.setByStagesCapacity(
                ObjectUtils.defaultIfNull(freeDepositData.getByStagesCapacity(), CommonConstant.ZERO) + ObjectUtils.defaultIfNull(params.getByStagesCapacity(),
                        CommonConstant.ZERO));
        FreeDepositRechargeRecord rechargeRecord = buildFreeDepositRechargeRecord(freeDepositData, FreeDepositData.FREE_TYPE_FY);
        
        if (hasData) {
            if (freeDepositDataMapper.update(freeDepositData) > 0) {
                freeDepositRechargeRecordService.insert(rechargeRecord);
            }
            return Pair.of(true, "");
        }
        
        if (freeDepositDataMapper.insertOne(freeDepositData) > 0) {
            freeDepositRechargeRecordService.insert(rechargeRecord);
        }
        return Pair.of(true, "");
    }
    
    @Override
    public Triple<Boolean, String, Object> recharge(FreeDepositDataQuery freeDepositDataQuery) {
        FreeDepositData freeDepositData = this.selectByTenantId(freeDepositDataQuery.getTenantId());
        if (Objects.isNull(freeDepositData)) {
            FreeDepositData freeDepositDataInsert = new FreeDepositData();
            freeDepositDataInsert.setFreeDepositCapacity(freeDepositDataQuery.getFreeDepositCapacity());
            freeDepositDataInsert.setTenantId(freeDepositDataQuery.getTenantId());
            freeDepositDataInsert.setDelFlag(FreeDepositData.DEL_NORMAL);
            freeDepositDataInsert.setRechargeTime(System.currentTimeMillis());
            freeDepositDataInsert.setCreateTime(System.currentTimeMillis());
            freeDepositDataInsert.setUpdateTime(System.currentTimeMillis());
            this.freeDepositDataMapper.insertOne(freeDepositDataInsert);
            
            //保存充值记录
            freeDepositRechargeRecordService.insert(buildFreeDepositRechargeRecord(freeDepositDataInsert, FreeDepositData.FREE_TYPE_PXZ));
            return Triple.of(true, "", null);
        }
        
        FreeDepositData freeDepositDataUpdate = new FreeDepositData();
        freeDepositDataUpdate.setId(freeDepositData.getId());
        freeDepositDataUpdate.setFreeDepositCapacity(freeDepositData.getFreeDepositCapacity() + freeDepositDataQuery.getFreeDepositCapacity());
        freeDepositDataUpdate.setRechargeTime(System.currentTimeMillis());
        freeDepositDataUpdate.setTenantId(freeDepositDataQuery.getTenantId());
        freeDepositDataUpdate.setUpdateTime(System.currentTimeMillis());
        this.freeDepositDataMapper.update(freeDepositDataUpdate);
        
        //保存充值记录
        freeDepositRechargeRecordService.insert(buildFreeDepositRechargeRecord(freeDepositDataUpdate, FreeDepositData.FREE_TYPE_PXZ));
        
        return Triple.of(true, "", null);
    }
    
    private FreeDepositRechargeRecord buildFreeDepositRechargeRecord(FreeDepositData freeDepositData, Integer freeType) {
        FreeDepositRechargeRecord freeDepositRechargeRecord = new FreeDepositRechargeRecord();
        freeDepositRechargeRecord.setFreeRecognizeCapacity(
                Objects.equals(FreeDepositData.FREE_TYPE_PXZ, freeType) ? freeDepositData.getFreeDepositCapacity() : freeDepositData.getFyFreeDepositCapacity());
        freeDepositRechargeRecord.setOperator(SecurityUtils.getUid());
        freeDepositRechargeRecord.setTenantId(freeDepositData.getTenantId());
        freeDepositRechargeRecord.setDelFlag(FreeDepositRechargeRecord.DEL_NORMAL);
        freeDepositRechargeRecord.setFreeType(freeType);
        freeDepositRechargeRecord.setByStagesCount(freeDepositData.getByStagesCapacity());
        freeDepositRechargeRecord.setCreateTime(System.currentTimeMillis());
        freeDepositRechargeRecord.setUpdateTime(System.currentTimeMillis());
        
        return freeDepositRechargeRecord;
    }
    
}
