package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.electricity.entity.City;
import com.xiliulou.electricity.mapper.CityMapper;
import com.xiliulou.electricity.service.CityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * (City)表服务实现类
 *
 * @author makejava
 * @since 2021-01-21 18:05:43
 */
@Service("cityService")
@Slf4j
public class CityServiceImpl implements CityService {
	@Resource
	private CityMapper cityMapper;

	/**
	 * 通过ID查询单条数据从DB
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public City queryByIdFromDB(Integer id) {
		return this.cityMapper.selectById(id);
	}


	@Override
	public City queryCityByCode(String code) {
		return cityMapper.selectOne(new LambdaQueryWrapper<City>().eq(City::getCode, code));
	}

	@Override
	public List<City> queryCityListByPid(Integer pid) {
		return cityMapper.selectList(new LambdaQueryWrapper<City>().eq(City::getPid, pid));
	}
}
