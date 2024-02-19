package com.xiliulou.electricity.mapper.merchant;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.merchant.ChannelEmployee;
import com.xiliulou.electricity.request.merchant.ChannelEmployeeRequest;
import com.xiliulou.electricity.vo.merchant.ChannelEmployeeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author BaoYu
 * @description: 渠道员工表
 * @date 2024/1/31 10:28
 */
public interface ChannelEmployeeMapper extends BaseMapper<ChannelEmployee> {
    
    ChannelEmployee selectById(@Param("id") Long id);
    
    ChannelEmployee selectByUid(@Param("uid") Long uid);
    
    List<ChannelEmployee> selectListByCondition(ChannelEmployeeRequest channelEmployeeRequest);
    
    Integer countByCondition(ChannelEmployeeRequest channelEmployeeRequest);
    
    List<ChannelEmployeeVO> selectChannelEmployees(ChannelEmployeeRequest channelEmployeeRequest);
    
    Integer insertOne(ChannelEmployee channelEmployee);
    
    Integer updateOne(ChannelEmployee channelEmployee);
    
    Integer removeById(@Param("id") Long id);
    
    
}
