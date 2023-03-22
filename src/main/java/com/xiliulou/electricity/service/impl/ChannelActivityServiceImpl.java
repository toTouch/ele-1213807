package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.ChannelActivity;
import com.xiliulou.electricity.mapper.ChannelActivityMapper;
import com.xiliulou.electricity.service.ChannelActivityService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.ChannelActivityVo;
import org.apache.commons.collections.ArrayStack;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 * (ChannelActivity)表服务实现类
 *
 * @author zgw
 * @since 2023-03-22 10:42:57
 */
@Service
@Slf4j
public class ChannelActivityServiceImpl implements ChannelActivityService {
    
    @Resource
    private ChannelActivityMapper channelActivityMapper;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ChannelActivity queryByIdFromDB(Long id) {
        return this.channelActivityMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ChannelActivity queryByIdFromCache(Long id) {
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
    public List<ChannelActivity> queryAllByLimit(int offset, int limit) {
        return this.channelActivityMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param channelActivity 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChannelActivity insert(ChannelActivity channelActivity) {
        this.channelActivityMapper.insertOne(channelActivity);
        return channelActivity;
    }
    
    /**
     * 修改数据
     *
     * @param channelActivity 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ChannelActivity channelActivity) {
        return this.channelActivityMapper.update(channelActivity);
        
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
        return this.channelActivityMapper.deleteById(id) > 0;
    }
    
    @Override
    public Triple<Boolean, String, Object> queryList(Long offset, Long size) {
        List<ChannelActivity> query = channelActivityMapper.queryList(offset, size, TenantContextHolder.getTenantId());
        List<ChannelActivityVo> voList = new ArrayList<>();
        Optional.ofNullable(query).orElse(new ArrayList<>()).forEach(item -> {
            ChannelActivityVo vo = new ChannelActivityVo();
            BeanUtils.copyProperties(item, vo);
            voList.add(vo);
        });
        return Triple.of(true, null, voList);
    }
}
