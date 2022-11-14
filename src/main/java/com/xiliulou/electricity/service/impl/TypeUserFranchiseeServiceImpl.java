package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.service.*;
import com.xiliulou.security.bean.TokenUser;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service("typeUserFranchiseeService")
public class TypeUserFranchiseeServiceImpl implements UserTypeService {
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    StoreService storeService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Autowired
    UserDataScopeService userDataScopeService;

    @Override
    public List<Integer> getEleIdListByUserType(TokenUser user) {
        //1、先找到加盟商
        Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
        if (ObjectUtil.isEmpty(franchisee)) {
            return null;
        }

        List<Store> storeList = storeService.queryByFranchiseeId(franchisee.getId());

        if (ObjectUtil.isEmpty(storeList)) {
            return null;
        }
        //2、再找加盟商绑定的门店
        List<Long> storeIdList = new ArrayList<>();
        for (Store store : storeList) {
            storeIdList.add(store.getId());
        }
        if (ObjectUtil.isEmpty(storeIdList)) {
            return null;
        }

        //3、再找门店绑定的柜子
        List<Integer> eleIdList = new ArrayList<>();
        for (Long storeId : storeIdList) {
            List<ElectricityCabinet> electricityCabinetList = electricityCabinetService.queryByStoreId(storeId);

            if (ObjectUtil.isNotEmpty(electricityCabinetList)) {
                for (ElectricityCabinet electricityCabinet : electricityCabinetList) {
                    eleIdList.add(electricityCabinet.getId());
                }
            }

        }

        return eleIdList;
    }

    @Override
    public List<Integer> getEleIdListByDataType(TokenUser user) {
        List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
        if (CollectionUtils.isEmpty(franchiseeIds)) {
            return Collections.EMPTY_LIST;
        }

        List<Store> storeList = storeService.selectByFranchiseeIds(franchiseeIds);
        if (CollectionUtils.isEmpty(storeList)) {
            return Collections.EMPTY_LIST;
        }

        List<Long> storeIds = storeList.stream().map(Store::getId).collect(Collectors.toList());
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
}
