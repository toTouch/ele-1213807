package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.StoreDetail;
import com.xiliulou.electricity.mapper.StoreDetailMapper;
import com.xiliulou.electricity.service.StoreDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * 门店详情表(StoreDetail)表服务实现类
 *
 * @author zzlong
 * @since 2022-12-14 13:54:40
 */
@Service("storeDetailService")
@Slf4j
public class StoreDetailServiceImpl implements StoreDetailService {
    @Autowired
    private StoreDetailMapper storeDetailMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public StoreDetail selectByIdFromDB(Long id) {
        return this.storeDetailMapper.selectById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public StoreDetail selectByIdFromCache(Long id) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<StoreDetail> selectByPage(int offset, int limit) {
        return this.storeDetailMapper.selectByPage(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param storeDetail 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StoreDetail insert(StoreDetail storeDetail) {
        if(Objects.isNull(storeDetail)){
            return storeDetail;
        }

        this.storeDetailMapper.insertOne(storeDetail);
        return storeDetail;
    }

    /**
     * 修改数据
     *
     * @param storeDetail 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(StoreDetail storeDetail) {
        return this.storeDetailMapper.update(storeDetail);

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
        return this.storeDetailMapper.deleteById(id) > 0;
    }

    @Override
    public int deleteByStoreId(Long id) {
        return this.storeDetailMapper.deleteByStoreId(id);
    }
}
