package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.Province;
import com.xiliulou.electricity.mapper.ProvinceMapper;
import com.xiliulou.electricity.service.ProvinceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        return this.provinceMapper.selectById(id);
    }


    @Override
    public List<Province> queryList() {
        return this.provinceMapper.queryAllCity();
    }
}
