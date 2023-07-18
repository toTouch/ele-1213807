package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.ElePower;
import com.xiliulou.electricity.mapper.ElePowerMapper;
import com.xiliulou.electricity.service.ElePowerService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
/**
 * (ElePower)表服务实现类
 *
 * @author makejava
 * @since 2023-07-18 10:20:44
 */
@Service("elePowerService")
@Slf4j
public class ElePowerServiceImpl implements ElePowerService {
    @Resource
    private ElePowerMapper elePowerMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElePower queryByIdFromDB(Long id) {
        return this.elePowerMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public  ElePower queryByIdFromCache(Long id) {
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
    public List<ElePower> queryAllByLimit(int offset, int limit) {
        return this.elePowerMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param elePower 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElePower insert(ElePower elePower) {
        this.elePowerMapper.insertOne(elePower);
        return elePower;
    }

    /**
     * 修改数据
     *
     * @param elePower 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ElePower elePower) {
       return this.elePowerMapper.update(elePower);
         
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
        return this.elePowerMapper.deleteById(id) > 0;
    }
}
