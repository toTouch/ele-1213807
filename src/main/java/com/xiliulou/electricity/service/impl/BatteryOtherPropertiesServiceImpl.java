package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.electricity.entity.BatteryOtherProperties;
import com.xiliulou.electricity.mapper.BatteryOtherPropertiesMapper;
import com.xiliulou.electricity.service.BatteryOtherPropertiesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * (City)表服务实现类
 *
 * @author makejava
 * @since 2021-01-21 18:05:43
 */
@Service
@Slf4j
public class BatteryOtherPropertiesServiceImpl implements BatteryOtherPropertiesService {
	@Resource
	BatteryOtherPropertiesMapper batteryOtherPropertiesMapper;

	@Override
	public void insertOrUpdate(BatteryOtherProperties batteryOtherProperties) {
		BatteryOtherProperties oldBatteryOtherProperties = batteryOtherPropertiesMapper.selectOne(new LambdaQueryWrapper<BatteryOtherProperties>()
				.eq(BatteryOtherProperties::getBatteryName, batteryOtherProperties.getBatteryName()));

		if(Objects.nonNull(oldBatteryOtherProperties)){
			batteryOtherProperties.setId(oldBatteryOtherProperties.getId());
			batteryOtherProperties.setUpdateTime(batteryOtherProperties.getCreateTime());
			batteryOtherPropertiesMapper.updateById(batteryOtherProperties);
		}else {
			batteryOtherProperties.setCreateTime(batteryOtherProperties.getCreateTime());
			batteryOtherProperties.setUpdateTime(batteryOtherProperties.getCreateTime());
			batteryOtherPropertiesMapper.insert(batteryOtherProperties);
		}


	}
}
