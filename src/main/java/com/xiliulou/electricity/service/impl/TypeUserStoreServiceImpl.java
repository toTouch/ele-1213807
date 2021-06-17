package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.UserTypeService;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service("typeUserStoreService")
public class TypeUserStoreServiceImpl implements UserTypeService {
	@Autowired
	StoreService storeService;
	@Autowired
	ElectricityCabinetService electricityCabinetService;

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
}
