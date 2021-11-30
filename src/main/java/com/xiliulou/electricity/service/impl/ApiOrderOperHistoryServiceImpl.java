package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.ApiOrderOperHistory;
import com.xiliulou.electricity.mapper.ApiOrderOperHistoryMapper;
import com.xiliulou.electricity.service.ApiOrderOperHistoryService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
/**
 * (ApiOrderOperHistory)表服务实现类
 *
 * @author makejava
 * @since 2021-11-09 16:57:54
 */
@Service("apiOrderOperHistoryService")
@Slf4j
public class ApiOrderOperHistoryServiceImpl implements ApiOrderOperHistoryService {
    @Resource
    private ApiOrderOperHistoryMapper apiOrderOperHistoryMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ApiOrderOperHistory queryByIdFromDB(Long id) {
        return this.apiOrderOperHistoryMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public  ApiOrderOperHistory queryByIdFromCache(Long id) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<ApiOrderOperHistory> queryAllByLimit(int offset, int limit) {
        return this.apiOrderOperHistoryMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param apiOrderOperHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiOrderOperHistory insert(ApiOrderOperHistory apiOrderOperHistory) {
        this.apiOrderOperHistoryMapper.insertOne(apiOrderOperHistory);
        return apiOrderOperHistory;
    }

    /**
     * 修改数据
     *
     * @param apiOrderOperHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ApiOrderOperHistory apiOrderOperHistory) {
       return this.apiOrderOperHistoryMapper.update(apiOrderOperHistory);
         
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.apiOrderOperHistoryMapper.deleteById(id) > 0;
    }

    @Override
    public List<ApiOrderOperHistory> queryByOrderId(String orderId, Integer orderTypeExchange) {
        return this.apiOrderOperHistoryMapper.queryByOrderId(orderId,orderTypeExchange);
    }
}
