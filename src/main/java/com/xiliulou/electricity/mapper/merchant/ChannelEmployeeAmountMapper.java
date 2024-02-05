package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.ChannelEmployeeAmount;

/**
 * @author BaoYu
 * @description:
 * @date 2024/1/31 20:36
 */
public interface ChannelEmployeeAmountMapper {
    
    ChannelEmployeeAmount selectById(Long id);
    
    ChannelEmployeeAmount selectByUid(Long uid);
    
    Integer updateOne(ChannelEmployeeAmount channelEmployeeAmount);
    
    Integer insertOne(ChannelEmployeeAmount channelEmployeeAmount);

}
