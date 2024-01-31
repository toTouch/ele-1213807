package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.request.merchant.ChannelEmployeeRequest;
import com.xiliulou.electricity.vo.merchant.ChannelEmployeeVO;

import java.util.List;

/**
 * @author BaoYu
 * @description:
 * @date 2024/1/31 15:43
 */
public interface ChannelEmployeeService {
    
    /**
     * 根据ID查询渠道员工信息
     * @param id
     * @return
     */
    ChannelEmployeeVO queryById(Long id);
    
    /**
     * 查询渠道员工列表
     * @param channelEmployeeRequest
     * @return
     */
    List<ChannelEmployeeVO> listChannelEmployee(ChannelEmployeeRequest channelEmployeeRequest);
    
    /**
     * 查询渠道员工总数量
     * @param channelEmployeeRequest
     * @return
     */
    Integer countChannelEmployee(ChannelEmployeeRequest channelEmployeeRequest);
    
    /**
     * 保存渠道员工信息
     * @param channelEmployeeRequest
     * @return
     */
    Integer saveChannelEmployee(ChannelEmployeeRequest channelEmployeeRequest);
    
    /**
     * 修改渠道员工信息
     * @param channelEmployeeRequest
     * @return
     */
    Integer updateChannelEmployee(ChannelEmployeeRequest channelEmployeeRequest);

}
