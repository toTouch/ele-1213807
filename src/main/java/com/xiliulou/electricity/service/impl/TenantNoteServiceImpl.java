package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
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
        
        TenantNote tenantNote = noteMapper.selectByTenantId(tenantId);
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
        TenantNote tenantNote = this.queryFromCacheByTenantId(rechargeRequest.getTenantId());
        
        // 新增或者修改
        TenantNote addNote = new TenantNote();
        addNote.setTenantId(rechargeRequest.getTenantId());
        addNote.setNoteNum(rechargeRequest.getRechargeNum());
        addNote.setRechargeTime(System.currentTimeMillis());
        addNote.setUpdateTime(System.currentTimeMillis());
        
        if (ObjectUtils.isEmpty(tenantNote)) {
            addNote.setCreateTime(System.currentTimeMillis());
            noteMapper.insertOne(addNote);
        } else {
            noteMapper.addNoteNum(addNote);
        }
        
        // 添加充值记录
        TenantNoteRecharge recharge = new TenantNoteRecharge();
        recharge.setTenantNoteId(recharge.getTenantNoteId());
        recharge.setRechargeNum(recharge.getRechargeNum());
        recharge.setRechargeTime(System.currentTimeMillis());
        recharge.setTenantNoteId(addNote.getId());
        recharge.setUid(uid);
        recharge.setTenantId(rechargeRequest.getTenantId());
        recharge.setCreateTime(System.currentTimeMillis());
        rechargeService.insertOne(recharge);
        
        return Triple.of(true, null, null);
    }
    
    @Override
    public void deleteCache(Integer tenantId) {
        redisService.delete(CacheConstant.CACHE_TENANT_NOTE + tenantId);
    }
}
