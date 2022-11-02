package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryOtherProperties;
import com.xiliulou.electricity.entity.BatteryOtherPropertiesQuery;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.mapper.BatteryOtherPropertiesMapper;
import com.xiliulou.electricity.service.BatteryOtherPropertiesService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
	@Autowired
	ElectricityBatteryService electricityBatteryService;
	@Autowired
	ElectricityCabinetBoxService electricityCabinetBoxService;

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
		
		BatteryOtherPropertiesQuery batteryOtherPropertiesQuery=new BatteryOtherPropertiesQuery();
		
		ElectricityBattery electricityBattery=electricityBatteryService.queryBySn(sn);
		if(Objects.isNull(electricityBattery) || !Objects.equals(electricityBattery.getTenantId(), TenantContextHolder.getTenantId())){
			return R.ok(batteryOtherPropertiesQuery);
		}
		
		
		if (Objects.equals(electricityBattery.getPhysicsStatus(),ElectricityBattery.PHYSICS_STATUS_WARE_HOUSE)){
			ElectricityCabinetBox electricityCabinetBox=electricityCabinetBoxService.selectByBatteryId(electricityBattery.getId());
			if (Objects.nonNull(electricityCabinetBox)) {
				batteryOtherPropertiesQuery.setChargeV(electricityCabinetBox.getChargeV());
			}
		}
		
		
		BatteryOtherProperties batteryOtherProperties=batteryOtherPropertiesMapper.selectOne(new LambdaQueryWrapper<BatteryOtherProperties>()
				.eq(BatteryOtherProperties::getBatteryName,sn));
		if(Objects.isNull(batteryOtherProperties)) {
			return R.ok(batteryOtherPropertiesQuery);
		}



		BeanUtils.copyProperties(batteryOtherProperties,batteryOtherPropertiesQuery);
		batteryOtherPropertiesQuery.setBatteryCoreVList(JsonUtil.fromJson(batteryOtherProperties.getBatteryCoreVList(),List.class));
		return R.ok(batteryOtherPropertiesQuery);
	}
}
