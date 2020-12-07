package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.mapper.StoreMapper;
import com.xiliulou.electricity.service.StoreService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;

/**
 * 门店表(TStore)表服务实现类
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@Service("tStoreService")
public class StoreServiceImpl implements StoreService {
    @Resource
    private StoreMapper storeMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Store queryByIdFromDB(Integer id) {
        return this.storeMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Store queryByIdFromCache(Integer id) {
        return null;
    }
    
    /**
     * 新增数据
     *
     * @param store 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Store insert(Store store) {
        this.storeMapper.insert(store);
        return store;
    }

    /**
     * 修改数据
     *
     * @param store 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(Store store) {
       return this.storeMapper.update(store);
         
    }
}