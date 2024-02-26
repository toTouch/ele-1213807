package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeDailyRecord;
import com.xiliulou.electricity.mapper.merchant.MerchantPlaceFeeDailyRecordMapper;
import com.xiliulou.electricity.service.merchant.MerchantPlaceFeeDailyRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName : MerchantPlaceFeeDailyRecordServiceImpl
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-19
 */
@Service
public class MerchantPlaceFeeDailyRecordServiceImpl implements MerchantPlaceFeeDailyRecordService {
    
    @Resource
    private MerchantPlaceFeeDailyRecordMapper merchantPlaceFeeDailyRecordMapper;
    
}
