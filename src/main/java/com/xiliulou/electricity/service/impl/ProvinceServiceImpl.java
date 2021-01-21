package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.electricity.entity.Province;
import com.xiliulou.electricity.mapper.ProvinceMapper;
import com.xiliulou.electricity.service.ProvinceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;

/**
 * (Province)表服务实现类
 *
 * @author makejava
 * @since 2021-01-21 18:05:46
 */
@Service("provinceService")
@Slf4j
public class ProvinceServiceImpl implements ProvinceService {
    @Resource
    private ProvinceMapper provinceMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Province queryByIdFromDB(Integer id) {
        return this.provinceMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public  Province queryByIdFromCache(Integer id) {
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
    public List<Province> queryAllByLimit(int offset, int limit) {
        return this.provinceMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param province 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Province insert(Province province) {
        this.provinceMapper.insert(province);
        return province;
    }

    /**
     * 修改数据
     *
     * @param province 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(Province province) {
       return this.provinceMapper.update(province);
         
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
        return this.provinceMapper.deleteById(id) > 0;
    }

    @Override
    public List<Province> queryList() {
        return this.provinceMapper.queryAllCity();
    }
}