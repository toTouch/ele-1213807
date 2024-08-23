/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/23
 */

package com.xiliulou.electricity.service.impl.profitsharing;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingConfig;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingReceiverConfig;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingReceiverConfigMapper;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingReceiverOptConfigRequest;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingConfigService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingReceiverConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/23 14:41
 */
@Service
public class ProfitSharingReceiverConfigServiceImpl implements ProfitSharingReceiverConfigService {
    
    @Resource
    private ProfitSharingReceiverConfigMapper profitSharingReceiverConfigMapper;
    
    @Resource
    private ProfitSharingConfigService profitSharingConfigService;
    
    @Resource
    private RedisService redisService;
    
    @Slave
    @Override
    public List<ProfitSharingReceiverConfig> queryListByProfitSharingConfigId(Integer tenantId, Long profitSharingConfigId) {
        return profitSharingReceiverConfigMapper.selectListByTenantIdAndProfitSharingConfigId(tenantId, profitSharingConfigId);
    }
    
    @Override
    public void insert(ProfitSharingReceiverOptConfigRequest request) {
        // 校验幂等
        this.checkIdempotent(request.getProfitSharingConfigId());
        
        Long profitSharingConfigId = request.getProfitSharingConfigId();
        ProfitSharingConfig profitSharingConfig = profitSharingConfigService.queryById(request.getTenantId(), profitSharingConfigId);
        if (Objects.isNull(profitSharingConfig)) {
            throw new BizException("分账方配置不存在");
        }
    
        Integer receiverType = request.getReceiverType();
        
    
    
    }
    
    
    /**
     * 幂等校验
     *
     * @param profitSharingConfigId
     * @author caobotao.cbt
     * @date 2024/8/23 08:54
     */
    private void checkIdempotent(Long profitSharingConfigId) {
        boolean b = redisService.setNx(String.format(CacheConstant.PROFIT_SHARING_RECEIVER_IDEMPOTENT_KEY, profitSharingConfigId), "1", 3000L, true);
        if (!b) {
            throw new BizException("频繁操作");
        }
    }
}
