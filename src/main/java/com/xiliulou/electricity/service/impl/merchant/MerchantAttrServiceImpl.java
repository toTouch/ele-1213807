package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.entity.merchant.MerchantAttr;
import com.xiliulou.electricity.mapper.merchant.MerchantAttrMapper;
import com.xiliulou.electricity.request.merchant.MerchantAttrRequest;
import com.xiliulou.electricity.service.merchant.MerchantAttrService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
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
    
    @Override
    public MerchantAttr queryById(Long id) {
        return this.merchantAttrMapper.selectById(id);
    }
    
    @Override
    public MerchantAttr queryByMerchantId(Long merchantId) {
        return this.merchantAttrMapper.selectByMerchantId(merchantId);
    }
    
    @Override
    public MerchantAttr queryByMerchantIdFromCache(Long merchantId) {
        MerchantAttr cacheMerchantAttr = redisService.getWithHash(CacheConstant.CACHE_MERCHANT_ATTR + merchantId, MerchantAttr.class);
        if (Objects.nonNull(cacheMerchantAttr)) {
            return cacheMerchantAttr;
        }
        
        MerchantAttr merchantAttr = this.queryByMerchantId(merchantId);
        if (Objects.isNull(merchantAttr)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_MERCHANT_ATTR + merchantId, merchantAttr);
        return merchantAttr;
    }
    
    @Override
    public Integer updateByMerchantId(MerchantAttr merchantAttr) {
        int update = this.merchantAttrMapper.updateByMerchantId(merchantAttr);
        
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> redisService.delete(CacheConstant.CACHE_MERCHANT_ATTR + merchantAttr.getMerchantId()));
        
        return update;
    }
    
    @Override
    public Integer insert(MerchantAttr merchantAttr) {
        return this.merchantAttrMapper.insert(merchantAttr);
    }
    
    @Override
    public Integer deleteByMerchantId(Long merchantId) {
        int delete = this.merchantAttrMapper.deleteByMerchantId(merchantId);
        
        DbUtils.dbOperateSuccessThenHandleCache(delete, i -> redisService.delete(CacheConstant.CACHE_MERCHANT_ATTR + merchantId));
        
        return delete;
    }
    
    @Override
    public Triple<Boolean, String, Object> updateUpgradeCondition(Long merchantId, Integer condition) {
        MerchantAttr merchantAttr = this.queryByMerchantIdFromCache(merchantId);
        if (Objects.isNull(merchantAttr) || !Objects.equals(merchantAttr.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(true, null, null);
        }
        
        MerchantAttr merchantAttrUpdate = new MerchantAttr();
        merchantAttrUpdate.setMerchantId(merchantAttr.getMerchantId());
        merchantAttrUpdate.setUpgradeCondition(condition);
        merchantAttrUpdate.setUpdateTime(System.currentTimeMillis());
        this.updateByMerchantId(merchantAttrUpdate);
        return Triple.of(true, null, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> updateInvitationCondition(MerchantAttrRequest request) {
        MerchantAttr merchantAttr = this.queryByMerchantIdFromCache(request.getMerchantId());
        if (Objects.isNull(merchantAttr) || !Objects.equals(merchantAttr.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(true, null, null);
        }
        
        MerchantAttr merchantAttrUpdate = new MerchantAttr();
        merchantAttrUpdate.setMerchantId(merchantAttr.getMerchantId());
        merchantAttrUpdate.setInvitationValidTime(request.getInvitationValidTime());
        merchantAttrUpdate.setValidTimeUnit(request.getValidTimeUnit());
        merchantAttrUpdate.setInvitationProtectionTime(request.getInvitationProtectionTime());
        merchantAttrUpdate.setProtectionTimeUnit(request.getProtectionTimeUnit());
        merchantAttrUpdate.setUpdateTime(System.currentTimeMillis());
        this.updateByMerchantId(merchantAttrUpdate);
        return Triple.of(true, null, null);
    }
    
    @Override
    public Integer initMerchantAttr(Long merchantId, Integer tenantId) {
        MerchantAttr merchantAttr = new MerchantAttr();
        merchantAttr.setUpgradeCondition(null);
        merchantAttr.setInvitationValidTime(24);
        merchantAttr.setValidTimeUnit(CommonConstant.TIME_UNIT_HOURS);
        merchantAttr.setInvitationProtectionTime(1);
        merchantAttr.setProtectionTimeUnit(CommonConstant.TIME_UNIT_HOURS);
        merchantAttr.setDelFlag(CommonConstant.DEL_N);
        merchantAttr.setMerchantId(merchantId);
        merchantAttr.setTenantId(tenantId);
        merchantAttr.setCreateTime(System.currentTimeMillis());
        merchantAttr.setUpdateTime(System.currentTimeMillis());
        return this.insert(merchantAttr);
    }
}
