package com.xiliulou.electricity.service.impl.merchant;

import cn.hutool.core.map.MapUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.entity.merchant.MerchantAttr;
import com.xiliulou.electricity.mapper.merchant.MerchantAttrMapper;
import com.xiliulou.electricity.request.merchant.MerchantAttrRequest;
import com.xiliulou.electricity.service.merchant.MerchantAttrService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 商户升级配置(MerchantAttr)表服务实现类
 *
 * @author zzlong
 * @since 2024-02-04 09:14:33
 */
@Service("merchantAttrService")
@Slf4j
public class MerchantAttrServiceImpl implements MerchantAttrService {
    
    @Resource
    private MerchantAttrMapper merchantAttrMapper;
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private OperateRecordUtil operateRecordUtil;
    
    @Override
    public MerchantAttr queryById(Long id) {
        return this.merchantAttrMapper.selectById(id);
    }
    
    @Override
    public MerchantAttr queryByTenantId(Integer tenantId) {
        return this.merchantAttrMapper.selectByTenantId(tenantId);
    }
    
    @Override
    public MerchantAttr queryByTenantIdFromCache(Integer tenantId) {
        MerchantAttr cacheMerchantAttr = redisService.getWithHash(CacheConstant.CACHE_MERCHANT_ATTR + tenantId, MerchantAttr.class);
        if (Objects.nonNull(cacheMerchantAttr)) {
            return cacheMerchantAttr;
        }
        
        MerchantAttr merchantAttr = this.queryByTenantId(tenantId);
        if (Objects.isNull(merchantAttr)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_MERCHANT_ATTR + tenantId, merchantAttr);
        return merchantAttr;
    }
    
    @Override
    public Integer updateByTenantId(MerchantAttr merchantAttr, Integer tenantId) {
        merchantAttr.setTenantId(tenantId);
        int update = this.merchantAttrMapper.updateByTenantId(merchantAttr);
        
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> redisService.delete(CacheConstant.CACHE_MERCHANT_ATTR + tenantId));
        
        return update;
    }
    
    @Override
    public Integer insert(MerchantAttr merchantAttr) {
        return this.merchantAttrMapper.insert(merchantAttr);
    }
    
    @Override
    public Integer deleteByTenantId(Integer tenantId) {
        int delete = this.merchantAttrMapper.deleteByTenantId(tenantId);
        
        DbUtils.dbOperateSuccessThenHandleCache(delete, i -> redisService.delete(CacheConstant.CACHE_MERCHANT_ATTR + tenantId));
        
        return delete;
    }
    
    @Override
    public MerchantAttr queryUpgradeCondition(Integer tenantId) {
        MerchantAttr merchantAttr = this.queryByTenantIdFromCache(tenantId);
        if (Objects.isNull(merchantAttr) || !Objects.equals(TenantContextHolder.getTenantId(), merchantAttr.getTenantId())) {
            return null;
        }
        
        return merchantAttr;
    }
    
    @Override
    public Triple<Boolean, String, Object> updateUpgradeCondition(Integer tenantId, Integer condition) {
        MerchantAttr merchantAttr = this.queryByTenantIdFromCache(tenantId);
        if (Objects.isNull(merchantAttr) || !Objects.equals(merchantAttr.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(true, null, null);
        }
        
        MerchantAttr merchantAttrUpdate = new MerchantAttr();
        merchantAttrUpdate.setUpgradeCondition(condition);
        merchantAttrUpdate.setUpdateTime(System.currentTimeMillis());
        this.updateByTenantId(merchantAttrUpdate, tenantId);
        return Triple.of(true, null, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> updateInvitationCondition(MerchantAttrRequest request) {
        MerchantAttr merchantAttr = this.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(merchantAttr) || !Objects.equals(merchantAttr.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(true, null, null);
        }
        
        MerchantAttr merchantAttrUpdate = new MerchantAttr();
        merchantAttrUpdate.setTenantId(TenantContextHolder.getTenantId());
        merchantAttrUpdate.setInvitationValidTime(request.getInvitationValidTime());
        merchantAttrUpdate.setValidTimeUnit(request.getValidTimeUnit());
        merchantAttrUpdate.setInvitationProtectionTime(request.getInvitationProtectionTime());
        merchantAttrUpdate.setProtectionTimeUnit(request.getProtectionTimeUnit());
        merchantAttrUpdate.setUpdateTime(System.currentTimeMillis());
        this.updateByTenantId(merchantAttrUpdate, TenantContextHolder.getTenantId());
        return Triple.of(true, null, null);
    }
    
    @Override
    public Boolean checkInvitationTime(MerchantAttr merchantAttr, Long invitationTime) {
        if (Objects.isNull(merchantAttr.getInvitationValidTime()) || Objects.isNull(merchantAttr.getValidTimeUnit())) {
            log.error("ELE ERROR!merchantAttr is illegal,tenantId={}", merchantAttr.getTenantId());
            return Boolean.FALSE;
        }
        
        Long validTime = merchantAttr.getInvitationValidTime() * (Objects.equals(merchantAttr.getValidTimeUnit(), MerchantConstant.INVITATION_TIME_UNIT_MINUTES) ? 1000 * 60L
                : 1000 * 60 * 60L);
        
        if (System.currentTimeMillis() > (invitationTime + validTime)) {
            log.error("ELE ERROR!invitation is expired,tenantId={}", merchantAttr.getTenantId());
            return Boolean.FALSE;
        }
        
        return Boolean.TRUE;
    }
    
    @Override
    public Integer initMerchantAttr(Integer tenantId) {
        MerchantAttr merchantAttr = new MerchantAttr();
        merchantAttr.setUpgradeCondition(MerchantConstant.UPGRADE_CONDITION_ALL);
        merchantAttr.setInvitationValidTime(24);
        merchantAttr.setValidTimeUnit(CommonConstant.TIME_UNIT_HOURS);
        merchantAttr.setInvitationProtectionTime(1);
        merchantAttr.setProtectionTimeUnit(CommonConstant.TIME_UNIT_HOURS);
        merchantAttr.setDelFlag(CommonConstant.DEL_N);
        merchantAttr.setTenantId(tenantId);
        merchantAttr.setCreateTime(System.currentTimeMillis());
        merchantAttr.setUpdateTime(System.currentTimeMillis());
        return this.insert(merchantAttr);
    }
    
    @Override
    public Triple<Boolean, String, Object> updateChannelSwitchState(Integer tenantId, Integer status) {
        MerchantAttr merchantAttr = this.queryByTenantIdFromCache(tenantId);
        if (Objects.isNull(merchantAttr) || !Objects.equals(merchantAttr.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(true, null, null);
        }
        
        MerchantAttr merchantAttrUpdate = new MerchantAttr();
        merchantAttrUpdate.setStatus(status);
        merchantAttrUpdate.setUpdateTime(System.currentTimeMillis());
        this.updateByTenantId(merchantAttrUpdate, merchantAttr.getTenantId());
        // 记录操作
        operateRecordUtil.record(null, MapUtil.of("status",status));
        return Triple.of(true, null, null);
    }
}
