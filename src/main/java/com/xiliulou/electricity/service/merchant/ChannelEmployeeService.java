package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.request.merchant.ChannelEmployeeRequest;
import com.xiliulou.electricity.vo.merchant.ChannelEmployeeVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * @author BaoYu
 * @description:
 * @date 2024/1/31 15:43
 */
public interface ChannelEmployeeService {
    
    /**
     * 根据ID查询渠道员工信息，用于页面展示
     *
     * @param id
     * @param franchiseeId
     * @return
     */
    ChannelEmployeeVO queryById(Long id, List<Long> franchiseeId);
    
    /**
     * 根据uid查询渠道员工信息
     * @param uid
     * @return
     */
    ChannelEmployeeVO queryByUid(Long uid);
    
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
     * 根据条件，查询渠道员工信息
     * @param channelEmployeeRequest
     * @return
     */
    List<ChannelEmployeeVO> queryChannelEmployees(ChannelEmployeeRequest channelEmployeeRequest);
    
    /**
     * 保存渠道员工信息
     * @param channelEmployeeRequest
     * @return
     */
    Triple<Boolean, String, Object> saveChannelEmployee(ChannelEmployeeRequest channelEmployeeRequest);
    
    /**
     * 修改渠道员工信息
     * @param channelEmployeeRequest
     * @return
     */
    Triple<Boolean, String, Object> updateChannelEmployee(ChannelEmployeeRequest channelEmployeeRequest);
    
    Integer removeById(Long id, List<Long> franchiseeId);
    
    Integer existsByAreaId(Long id);
}
