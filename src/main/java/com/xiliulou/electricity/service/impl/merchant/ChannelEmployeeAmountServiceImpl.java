package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.merchant.ChannelEmployeeAmount;
import com.xiliulou.electricity.exception.BizException;
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
    private ChannelEmployeeAmountMapper channelEmployeeAmountMapper;
    
    @Resource
    UserService userService;
    
    @Transactional
    @Override
    public Integer addAmount(BigDecimal amount, Long uid, Long tenantId) {
       /* ChannelEmployeeAmount channelEmployeeAmount = channelEmployeeAmountMapper.selectByUid(uid, tenantId);
        if(Objects.isNull(channelEmployeeAmount)){
            log.error("add amount by uid error, not found channel employee amount info, uid = {}", uid);
            throw new BizException("120005", "渠道员余额账户不存在");
        }
        BigDecimal balance = channelEmployeeAmount.getBalance();
        ChannelEmployeeAmount channelEmployeeAmountUpdate = new ChannelEmployeeAmount();
        channelEmployeeAmountUpdate.setUid(uid);
        channelEmployeeAmountUpdate.setBalance(balance.add(amount));
        channelEmployeeAmountUpdate.setUpdateTime(System.currentTimeMillis());*/
        log.info("add amount for channel employee, amount = {}, uid = {}", amount, uid);
        User user = userService.queryByUidFromCache(uid);
        if(Objects.isNull(user)){
            log.error("add amount by uid error, not found channel employee user, uid = {}", uid);
            throw new BizException("120008", "渠道员不存在");
        }
        
        Integer result = channelEmployeeAmountMapper.addAmountByUid(amount, uid, tenantId, System.currentTimeMillis());
        return result;
    }
    
    @Transactional
    @Override
    public Integer reduceAmount(BigDecimal amount, Long uid, Long tenantId) {
        /*ChannelEmployeeAmount channelEmployeeAmount = channelEmployeeAmountMapper.selectByUid(uid, tenantId);
        if(Objects.isNull(channelEmployeeAmount)){
            log.error("reduce amount by uid error, not found channel employee amount info, uid = {}", uid);
            throw new BizException("120005", "渠道员余额账户不存在");
        }
        BigDecimal balance = channelEmployeeAmount.getBalance();
        
        if(balance.compareTo(amount) < 0){
            log.error("reduce amount by uid error, balance for channel employee not enough, uid = {}, balance = {}", uid, balance);
            throw new BizException("120006", "渠道员余额账户不足");
        }
        
        ChannelEmployeeAmount channelEmployeeAmountUpdate = new ChannelEmployeeAmount();
        channelEmployeeAmountUpdate.setUid(uid);
        channelEmployeeAmountUpdate.setBalance(balance.subtract(amount));
        channelEmployeeAmountUpdate.setUpdateTime(System.currentTimeMillis());*/
        
        log.info("reduce amount for channel employee, amount = {}, uid = {}", amount, uid);
        User user = userService.queryByUidFromCache(uid);
        if(Objects.isNull(user)){
            log.error("reduce amount by uid error, not found channel employee user, uid = {}", uid);
            throw new BizException("120008", "渠道员不存在");
        }
    
        Integer result = channelEmployeeAmountMapper.reduceAmountByUid(amount, uid, tenantId, System.currentTimeMillis());
        return result;
    }
}
