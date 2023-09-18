package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ChannelActivityHistory;
import com.xiliulou.electricity.query.ChannelActivityHistoryQuery;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * (ChannelActivityHistory)表服务接口
 *
 * @author Hardy
 * @since 2023-03-23 09:24:24
 */
public interface ChannelActivityHistoryService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    ChannelActivityHistory queryByIdFromDB(Long id);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ChannelActivityHistory queryByIdFromCache(Long id);
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<ChannelActivityHistory> queryAllByLimit(int offset, int limit);
    
    /**
     * 新增数据
     *
     * @param channelActivityHistory 实例对象
     * @return 实例对象
     */
    ChannelActivityHistory insert(ChannelActivityHistory channelActivityHistory);
    
    /**
     * 修改数据
     *
     * @param channelActivityHistory 实例对象
     * @return 实例对象
     */
    Integer update(ChannelActivityHistory channelActivityHistory);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);
    
    Long queryInviteCount(Long uid);
    
    ChannelActivityHistory queryByUid(Long uid);
    
    Triple<Boolean, String, Object> queryList(Long size, Long offset, String phone, Long beginTime, Long endTime);
    
    Triple<Boolean, String, Object> queryCount(String phone, Long beginTime, Long endTime);
    
    R queryCode();
    
    R scanIntoActivity(String code);
    
    void queryExportExcel(String phone, Long uid, Long beginTime, Long endTime, HttpServletResponse response);

    Triple<Boolean, String, Object> queryActivityHistoryList(ChannelActivityHistoryQuery channelActivityHistoryQuery);

    Triple<Boolean, String, Object> queryActivityHistoryCount(ChannelActivityHistoryQuery channelActivityHistoryQuery);

}
