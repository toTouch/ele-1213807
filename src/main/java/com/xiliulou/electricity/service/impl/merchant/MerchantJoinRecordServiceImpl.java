package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.merchant.MerchantJoinRecord;
import com.xiliulou.electricity.mapper.merchant.MerchantJoinRecordMapper;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.merchant.MerchantJoinRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author HeYafeng
 * @description 参与记录
 * @date 2024/2/6 18:04:22
 */
@Slf4j
@Service
public class MerchantJoinRecordServiceImpl implements MerchantJoinRecordService {
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private TenantService tenantService;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Resource
    private MerchantJoinRecordMapper merchantJoinRecordMapper;
    
    @Override
    public R joinScanCode(String code) {
        Long joinUid = SecurityUtils.getUid();
        
        if (!redisService.setNx(CacheConstant.CACHE_MERCHANT_SCAN_INTO_ACTIVITY_LOCK + joinUid, "1", 2000L, false)) {
            return R.fail(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
            if (Objects.isNull(tenant)) {
                log.error("MERCHANT JOIN ERROR! not found tenant,tenantId={}", TenantContextHolder.getTenantId());
                return R.fail("ELECTRICITY.00101", "找不到租户");
            }
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(joinUid);
            if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.error("MERCHANT JOIN ERROR! not found userInfo,uid={}", joinUid);
                return R.fail(false, "ELECTRICITY.0024", "用户已被禁用");
            }
            
            // 保护期内不能扫码
            Integer integer = this.existsInProtectionTimeByJoinUid(joinUid);
    
            // TODO 保存记录
            MerchantJoinRecord record = new MerchantJoinRecord();
            return R.ok(merchantJoinRecordMapper.insertOne(record));
        } finally {
            redisService.delete(CacheConstant.CACHE_MERCHANT_SCAN_INTO_ACTIVITY_LOCK + joinUid);
        }
    }
    
    @Slave
    @Override
    public Integer existsInProtectionTimeByJoinUid(Long joinUid) {
        return merchantJoinRecordMapper.existsInProtectionTimeByJoinUid(joinUid);
    }
    
    
}
