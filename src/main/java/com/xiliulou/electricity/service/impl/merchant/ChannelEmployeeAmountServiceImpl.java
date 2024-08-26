package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.merchant.ChannelEmployeeAmountMapper;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.merchant.ChannelEmployeeAmountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/20 16:22
 */

@Slf4j
@Service("ChannelEmployeeAmountService")
public class ChannelEmployeeAmountServiceImpl implements ChannelEmployeeAmountService {
    
    @Resource
    UserService userService;
    
    @Resource
    private ChannelEmployeeAmountMapper channelEmployeeAmountMapper;
    
    @Transactional
    @Override
    public Integer addAmount(BigDecimal amount, Long uid, Long tenantId) {
        User user = userService.queryByUidFromCache(uid);
        
        if (Objects.isNull(user)) {
            log.error("add amount by uid error, not found channel employee user, uid = {}", uid);
            //throw new BizException("120008", "渠道员不存在");
            return NumberConstant.ZERO;
        }
        
        Integer result = channelEmployeeAmountMapper.addAmountByUid(amount, uid, tenantId, System.currentTimeMillis());
        return result;
    }
    
    @Transactional
    @Override
    public Integer reduceAmount(BigDecimal amount, Long uid, Long tenantId) {
        User user = userService.queryByUidFromCache(uid);
        
        if (Objects.isNull(user)) {
            log.error("reduce amount by uid error, not found channel employee user, uid = {}", uid);
            return NumberConstant.ZERO;
        }
        
        Integer result = channelEmployeeAmountMapper.reduceAmountByUid(amount, uid, tenantId, System.currentTimeMillis());
        return result;
    }
}
