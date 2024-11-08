package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.TenantNote;
import com.xiliulou.electricity.entity.TenantNoteRecharge;
import com.xiliulou.electricity.mapper.TenantNoteMapper;
import com.xiliulou.electricity.request.tenantNote.TenantRechargeRequest;
import com.xiliulou.electricity.service.TenantNoteRechargeService;
import com.xiliulou.electricity.service.TenantNoteService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.utils.DbUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author maxiaodong
 * @date 2023/12/27 20:32
 * @desc
 */
@Service
@Slf4j
public class TenantNoteServiceImpl implements TenantNoteService {
    @Resource
    private TenantNoteMapper noteMapper;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private TenantNoteRechargeService rechargeService;
    
    @Resource
    private TenantService tenantService;
    
    @Slave
    @Override
    public TenantNote queryFromCacheByTenantId(Integer tenantId) {
        TenantNote cache = redisService.getWithHash(CacheConstant.CACHE_TENANT_NOTE + tenantId, TenantNote.class);
        if (Objects.nonNull(cache)) {
            return cache;
        }
        
        TenantNote tenantNote = this.queryFromDbByTenantId(tenantId);
        if (Objects.isNull(tenantNote)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_TENANT_NOTE + tenantId, tenantNote);
        return tenantNote;
    }
    
    @Override
    public void updateNoteNumById(TenantNote tenantNote) {
        int i = noteMapper.reduceNoteNum(tenantNote);
        DbUtils.dbOperateSuccessThenHandleCache(i, item -> {
            redisService.delete(CacheConstant.CACHE_TENANT_NOTE + tenantNote.getTenantId());
        });
    }
    
    /**
     * 短信充值
     *
     * @param rechargeRequest
     * @param uid
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> recharge(TenantRechargeRequest rechargeRequest, Long uid) {
        boolean result = redisService.setNx(CacheConstant.CACHE_TENANT_NOTE_RECHARGE_LOCK + uid, "1", 3 * 1000L, false);
        if (!result) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
    
        Tenant tenant = tenantService.queryByIdFromCache(rechargeRequest.getTenantId());
        if (ObjectUtils.isEmpty(tenant)) {
            return Triple.of(false, "ELECTRICITY.00101", "找不到租户");
        }
    
        // 检测数据是否存在
        TenantNote tenantNote = this.queryFromDbByTenantId(rechargeRequest.getTenantId());
        
        // 新增或者修改
        TenantNote addNote = new TenantNote();
        addNote.setTenantId(rechargeRequest.getTenantId());
        addNote.setNoteNum(rechargeRequest.getRechargeNum());
        addNote.setRechargeTime(System.currentTimeMillis());
        addNote.setUpdateTime(System.currentTimeMillis());
        
        Long num = rechargeRequest.getRechargeNum();
        if (ObjectUtils.isNotEmpty(tenantNote) && ObjectUtils.isNotEmpty(tenantNote.getNoteNum())) {
            num = tenantNote.getNoteNum();
        }
        
        if (num > NumberConstant.NOTE_MAX_NUM) {
            return Triple.of(false, "300831", "短信充值数量已超过最大限额，请下次再进行充值操作");
        }
        
        if (ObjectUtils.isEmpty(tenantNote)) {
            addNote.setCreateTime(System.currentTimeMillis());
            noteMapper.insertOne(addNote);
        } else {
            addNote.setId(tenantNote.getId());
            noteMapper.addNoteNum(addNote);
        }
        
        // 添加充值记录
        TenantNoteRecharge recharge = new TenantNoteRecharge();
        recharge.setRechargeNum(rechargeRequest.getRechargeNum());
        recharge.setRechargeTime(System.currentTimeMillis());
        recharge.setTenantNoteId(addNote.getId());
        recharge.setUid(uid);
        recharge.setTenantId(rechargeRequest.getTenantId());
        recharge.setCreateTime(System.currentTimeMillis());
        rechargeService.insertOne(recharge);
        
        return Triple.of(true, null, null);
    }
    
    @Slave
    @Override
    public TenantNote queryFromDbByTenantId(Integer tenantId) {
        TenantNote tenantNote = noteMapper.selectByTenantId(tenantId);
        return tenantNote;
    }
    
    @Override
    public void deleteCache(Integer tenantId) {
        redisService.delete(CacheConstant.CACHE_TENANT_NOTE + tenantId);
    }
    
    @Override
    public int reduceNoteNumById(TenantNote tenantNote) {
        int i = noteMapper.reduceNoteNumById(tenantNote);
        DbUtils.dbOperateSuccessThenHandleCache(i, item -> {
            redisService.delete(CacheConstant.CACHE_TENANT_NOTE + tenantNote.getTenantId());
        });
        
        return i;
    }
    
    @Override
    @Slave
    public List<TenantNote> listByTenantIdList(List<Integer> tenantIdList) {
        return noteMapper.selectListByTenantIdList(tenantIdList);
    }
}
