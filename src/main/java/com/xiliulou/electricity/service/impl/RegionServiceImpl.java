package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.Region;
import com.xiliulou.electricity.mapper.RegionMapper;
import com.xiliulou.electricity.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (Region)表服务实现类
 *
 * @author zzlong
 * @since 2022-12-12 11:38:20
 */
@Service("regionService")
@Slf4j
public class RegionServiceImpl implements RegionService {
    @Autowired
    private RegionMapper regionMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Region selectByIdFromDB(Integer id) {
        return this.regionMapper.selectById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param code 主键
     * @return 实例对象
     */
    @Override
    public Region selectByCode(String code) {
        return this.regionMapper.selectByCode(code);
    }


    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<Region> selectByPage(int offset, int limit) {
        return this.regionMapper.selectByPage(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param region 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Region insert(Region region) {
        this.regionMapper.insertOne(region);
        return region;
    }

    /**
     * 修改数据
     *
     * @param region 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(Region region) {
        return this.regionMapper.update(region);

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Integer id) {
        return this.regionMapper.deleteById(id) > 0;
    }
}
