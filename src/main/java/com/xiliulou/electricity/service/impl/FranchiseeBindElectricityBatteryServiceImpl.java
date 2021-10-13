package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.electricity.entity.FranchiseeBindElectricityBattery;
import com.xiliulou.electricity.mapper.FranchiseeBindElectricityBatteryMapper;
import com.xiliulou.electricity.service.FranchiseeBindElectricityBatteryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * (ElectricityBatteryBind)表服务实现类
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@Service
@Slf4j
public class FranchiseeBindElectricityBatteryServiceImpl implements FranchiseeBindElectricityBatteryService {
	@Resource
	FranchiseeBindElectricityBatteryMapper franchiseeBindElectricityBatteryMapper;

	@Override
	public void deleteByFranchiseeId(Integer franchiseeId) {
		franchiseeBindElectricityBatteryMapper.deleteByFranchiseeId(franchiseeId);
	}

	@Override
	public void insert(FranchiseeBindElectricityBattery franchiseeBindElectricityBattery) {
		franchiseeBindElectricityBatteryMapper.insert(franchiseeBindElectricityBattery);
	}

	@Override
	public List<FranchiseeBindElectricityBattery> queryByFranchiseeId(Long id) {
		return franchiseeBindElectricityBatteryMapper.selectList(new LambdaQueryWrapper<FranchiseeBindElectricityBattery>().eq(FranchiseeBindElectricityBattery::getFranchiseeId, id));
	}

	@Override
	public Integer queryCountByBattery(Long electricityBatteryId) {
		return franchiseeBindElectricityBatteryMapper.selectCount(new LambdaQueryWrapper<FranchiseeBindElectricityBattery>()
				.eq(FranchiseeBindElectricityBattery::getElectricityBatteryId, electricityBatteryId));
	}

	@Override
	public FranchiseeBindElectricityBattery queryByBatteryId(Long id) {
		return franchiseeBindElectricityBatteryMapper.selectOne(new LambdaQueryWrapper<FranchiseeBindElectricityBattery>()
				.eq(FranchiseeBindElectricityBattery::getElectricityBatteryId, id));
	}
}
