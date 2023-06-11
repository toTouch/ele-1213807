package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.City;
import com.xiliulou.electricity.entity.Province;
import com.xiliulou.electricity.entity.Region;
import com.xiliulou.electricity.mapper.CityMapper;
import com.xiliulou.electricity.mapper.ProvinceMapper;
import com.xiliulou.electricity.service.CityService;
import com.xiliulou.electricity.service.ProvinceService;
import com.xiliulou.electricity.service.RegionService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    CityMapper cityMapper;

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

    @Slave
    @Override
    public List<Province> queryList() {
        return this.provinceMapper.queryAllCity();
    }
}


