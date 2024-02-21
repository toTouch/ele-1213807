package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.merchant.MerchantPromotionFeeService;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionFeeHomeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @ClassName : MerchantPromotionFeeServiceImpl
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-21
 */
@Service
@Slf4j
public class MerchantPromotionFeeServiceImpl implements MerchantPromotionFeeService {
    
    @Override
    public R queryPromotionFeeHome(Integer type,Long uid) {
        MerchantPromotionFeeHomeVO merchantPromotionFeeHomeVO = new MerchantPromotionFeeHomeVO();
    
        //总电量统计
       /* CompletableFuture<Void> sumPower = CompletableFuture.runAsync(() -> {
            ElectricityCabinetPowerVo electricityCabinetPowerVo=electricityCabinetPowerMapper.queryLatestPower(electricityCabinetPowerQuery);
            if (Objects.nonNull(electricityCabinetPowerVo)) {
                electricityCabinetSumPowerVo.setSumPower(electricityCabinetPowerVo.getSumPower());
            }
        }, threadPool).exceptionally(e -> {
            log.error("query electricityCabinet sum power ERROR!", e);
            return null;
        });*/
        // 可提现金额
        
        //今日预估收入
        
        return null;
    }
}
