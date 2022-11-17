package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.electricity.entity.City;
import com.xiliulou.electricity.entity.ServiceFeeUserInfo;
import com.xiliulou.electricity.mapper.CityMapper;
import com.xiliulou.electricity.mapper.ServiceFeeUserInfoMapper;
import com.xiliulou.electricity.service.CityService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户停卡绑定(TServiceFeeUserInfo)实体类
 *
 * @author makejava
 * @since 2022-11-17 16:00:45
 */
@Service("serviceFeeUserInfoService")
@Slf4j
public class ServiceFeeUserInfoServiceImpl implements ServiceFeeUserInfoService {

	@Resource
	ServiceFeeUserInfoMapper serviceFeeUserInfoMapper;

	@Override
	public int insert(ServiceFeeUserInfo serviceFeeUserInfo) {
		return serviceFeeUserInfoMapper.insert(serviceFeeUserInfo);
	}

	@Override
	public int update(ServiceFeeUserInfo serviceFeeUserInfo) {
		return serviceFeeUserInfoMapper.update(serviceFeeUserInfo);
	}


}
