package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.electricity.request.merchant.ChannelEmployeeRequest;
import com.xiliulou.electricity.service.merchant.ChannelEmployeeService;
import com.xiliulou.electricity.vo.merchant.ChannelEmployeeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author BaoYu
 * @description:
 * @date 2024/1/31 16:13
 */

@Slf4j
@Service("channelEmployeeService")
public class ChannelEmployeeServiceImpl implements ChannelEmployeeService {
    
    @Override
    public ChannelEmployeeVO queryById(Long id) {
        return null;
    }
    
    @Override
    public List<ChannelEmployeeVO> listChannelEmployee(ChannelEmployeeRequest channelEmployeeRequest) {
        return null;
    }
    
    @Override
    public Integer countChannelEmployee(ChannelEmployeeRequest channelEmployeeRequest) {
        return null;
    }
    
    @Override
    public Integer saveChannelEmployee(ChannelEmployeeRequest channelEmployeeRequest) {
        return null;
    }
    
    @Override
    public Integer updateChannelEmployee(ChannelEmployeeRequest channelEmployeeRequest) {
        return null;
    }
}
