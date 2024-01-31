package com.xiliulou.electricity.mapper.merchant;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.merchant.ChannelEmployee;

import java.util.List;

/**
 * @author BaoYu
 * @description: 渠道员工表
 * @date 2024/1/31 10:28
 */
public interface ChannelEmployeeMapper extends BaseMapper<ChannelEmployee> {
    
    ChannelEmployee selectById(Long id);
    
    List<ChannelEmployee> selectListByCondition(ChannelEmployee channelEmployee);
    
    Integer countByCondition(ChannelEmployee channelEmployee);
    
    Integer insertOne(ChannelEmployee channelEmployee);
    
    Integer updateOne(ChannelEmployee channelEmployee);
    
    Integer removeById(ChannelEmployee channelEmployee);
    
    
}
