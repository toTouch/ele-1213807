package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.merchant.MerchantUserAmount;
import com.xiliulou.electricity.mapper.merchant.MerchantUserAmountMapper;
import com.xiliulou.electricity.query.merchant.MerchantUserAmountQueryMode;
import com.xiliulou.electricity.service.merchant.MerchantUserAmountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/18 19:37
 * @desc
 */
@Service("merchantUserAmountService")
@Slf4j
public class MerchantUserAmountServiceImpl implements MerchantUserAmountService {
    @Resource
    private MerchantUserAmountMapper merchantUserAmountMapper;
    
    @Transactional
    @Override
    public Integer save(MerchantUserAmount merchantUserAmount) {
        return merchantUserAmountMapper.insert(merchantUserAmount);
    }
    
    @Slave
    @Override
    public List<MerchantUserAmount> queryList(MerchantUserAmountQueryMode joinRecordQueryMode) {
        return merchantUserAmountMapper.queryList(joinRecordQueryMode);
    }
    
    @Transactional
    @Override
    public Integer addAmount(BigDecimal amount, Long uid, Long tenantId) {
        return merchantUserAmountMapper.addAmountByUid(amount, uid, tenantId);
    }
    
    @Transactional
    @Override
    public Integer reduceAmount(BigDecimal amount, Long uid, Long tenantId) {
        return merchantUserAmountMapper.reduceAmountByUid(amount, uid, tenantId);
    }
}
