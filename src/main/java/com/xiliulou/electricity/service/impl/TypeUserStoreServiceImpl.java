package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserTypeService;
import com.xiliulou.security.bean.TokenUser;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service("typeUserStoreService")
public class TypeUserStoreServiceImpl implements UserTypeService {
	@Autowired
	StoreService storeService;
	@Autowired
	ElectricityCabinetService electricityCabinetService;
	@Autowired
	UserDataScopeService userDataScopeService;
	
	@Autowired
	ElectricityCarService electricityCarService;
	
	
	@Override
	public List<Integer> getEleIdListByUserType(TokenUser user) {
		//2、再找门店绑定的门店
		Store store = storeService.queryByUid(user.getUid());
		if (Objects.isNull(store)) {
			return null;
		}

		//3、再找门店绑定的柜子
		List<Integer> eleIdList = new ArrayList<>();

		List<ElectricityCabinet> electricityCabinetList = electricityCabinetService.queryByStoreId(store.getId());

		if (ObjectUtil.isNotEmpty(electricityCabinetList)) {
			for (ElectricityCabinet electricityCabinet : electricityCabinetList) {
				eleIdList.add(electricityCabinet.getId());
			}
		}

		return eleIdList;
	}

	@Override
	public List<Integer> getEleIdListByDataType(TokenUser user) {
		List<Long> storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
		if (CollectionUtils.isEmpty(storeIds)) {
			return Collections.EMPTY_LIST;
		}

		List<ElectricityCabinet> electricityCabinetList =electricityCabinetService.selectBystoreIds(storeIds);
		if (CollectionUtils.isEmpty(electricityCabinetList)) {
			return Collections.EMPTY_LIST;
		}

		List<Integer> eleIds = electricityCabinetList.stream().map(ElectricityCabinet::getId).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(eleIds)) {
			return Collections.EMPTY_LIST;
		}

		return eleIds;
	}
	
	@Override
	public List<Integer> getCarIdListByyDataType(TokenUser user) {
		List<Long> storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
		if (CollectionUtils.isEmpty(storeIds)) {
			return Collections.EMPTY_LIST;
		}
		
		List<ElectricityCar> electricityCarList = electricityCarService.queryByStoreIds(storeIds);
		if (CollectionUtils.isEmpty(electricityCarList)) {
			return Collections.EMPTY_LIST;
		}
		
		List<Integer> carIds = electricityCarList.stream().map(ElectricityCar::getId).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(carIds)) {
			return Collections.EMPTY_LIST;
		}
		
		return carIds;
	}
}
