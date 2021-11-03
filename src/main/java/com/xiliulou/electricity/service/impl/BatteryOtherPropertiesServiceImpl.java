package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryOtherProperties;
import com.xiliulou.electricity.entity.BatteryOtherPropertiesQuery;
import com.xiliulou.electricity.mapper.BatteryOtherPropertiesMapper;
import com.xiliulou.electricity.service.BatteryOtherPropertiesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
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
			batteryOtherProperties.setUpdateTime(System.currentTimeMillis());
			batteryOtherPropertiesMapper.updateById(batteryOtherProperties);
		}else {
			batteryOtherProperties.setCreateTime(System.currentTimeMillis());
			batteryOtherProperties.setUpdateTime(System.currentTimeMillis());
			batteryOtherPropertiesMapper.insert(batteryOtherProperties);
		}


	}

	@Override
	public R queryBySn(String sn) {
		BatteryOtherProperties batteryOtherProperties=batteryOtherPropertiesMapper.selectOne(new LambdaQueryWrapper<BatteryOtherProperties>()
				.eq(BatteryOtherProperties::getBatteryName,sn));

		BatteryOtherPropertiesQuery batteryOtherPropertiesQuery=new BatteryOtherPropertiesQuery();
		BeanUtils.copyProperties(batteryOtherProperties,batteryOtherPropertiesQuery);
		batteryOtherPropertiesQuery.setBatteryCoreVList(JsonUtil.fromJson(batteryOtherProperties.getBatteryCoreVList(),List.class));
		return R.ok(batteryOtherPropertiesQuery);
	}
}
