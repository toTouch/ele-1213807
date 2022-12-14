package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.StoreTag;
import com.xiliulou.electricity.mapper.StoreTagMapper;
import com.xiliulou.electricity.query.StoreTagQuery;
import com.xiliulou.electricity.service.StoreTagService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 门店标签表(StoreTag)表服务实现类
 *
 * @author zzlong
 * @since 2022-12-14 13:55:07
 */
@Service("storeTagService")
@Slf4j
public class StoreTagServiceImpl implements StoreTagService {
    @Autowired
    private StoreTagMapper storeTagMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public StoreTag selectByIdFromDB(Long id) {
        return this.storeTagMapper.selectById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public StoreTag selectByIdFromCache(Long id) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @return 对象列表
     */
    @Override
    public List<StoreTag> selectByPage(StoreTagQuery storeTagQuery) {
        return this.storeTagMapper.selectByPage(storeTagQuery);
    }

    @Override
    public Integer selectPageCount(StoreTagQuery query) {
        return this.storeTagMapper.selectPageCount(query);
    }

    /**
     * 新增数据
     *
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer insert(StoreTagQuery storeTagQuery) {
        StoreTag storeTag = new StoreTag();
        BeanUtils.copyProperties(storeTagQuery,storeTag);
        storeTag.setStatus(StoreTag.STATUS_ENABLE);
        storeTag.setDelFlag(StoreTag.DEL_NORMAL);
        storeTag.setTenantId(TenantContextHolder.getTenantId());
        storeTag.setCreateTime(System.currentTimeMillis());
        storeTag.setUpdateTime(System.currentTimeMillis());

        return this.storeTagMapper.insertOne(storeTag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchInsert(List<StoreTag> storeTags) {
        if(CollectionUtils.isEmpty(storeTags)){
            log.error("ELE ERROR! storeTags is empty");
            return NumberConstant.ZERO;
        }

        return this.storeTagMapper.batchInsert(storeTags);
    }


    /**
     * 修改数据
     *
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(StoreTagQuery storeTagQuery) {
        StoreTag storeTag = new StoreTag();
        BeanUtils.copyProperties(storeTagQuery,storeTag);
        storeTag.setUpdateTime(System.currentTimeMillis());
        return this.storeTagMapper.update(storeTag);
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
        return this.storeTagMapper.deleteById(id) > 0;
    }

    @Override
    public int deleteByStoreId(Long id) {
        return this.storeTagMapper.deleteByStoreId(id);
    }
}
