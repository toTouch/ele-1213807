package com.xiliulou.electricity.service.impl;
import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.UserTypeService;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service("typeUserFranchiseeService")
public class TypeUserFranchiseeServiceImpl implements UserTypeService {
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    StoreService storeService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;

    @Override
    public List<Integer> getEleIdListByUserType(TokenUser user) {
        //1、先找到加盟商
        Franchisee franchisee=franchiseeService.queryByUid(user.getUid());
        if(ObjectUtil.isEmpty(franchisee)){
            return null;
        }

        List<Store> storeList= storeService.queryByFranchiseeId(franchisee.getId());

        if(ObjectUtil.isEmpty(storeList)){
            return null;
        }
        //2、再找加盟商绑定的门店
        List<Long> storeIdList=new ArrayList<>();
        for (Store store:storeList) {
            storeIdList.add(store.getId());
        }
        if(ObjectUtil.isEmpty(storeIdList)){
            return null;
        }

        //3、再找门店绑定的柜子
        List<Integer> eleIdList=new ArrayList<>();
        for (Long storeId:storeIdList) {
            List<ElectricityCabinet> electricityCabinetList=electricityCabinetService.queryByStoreId(storeId);

            if(ObjectUtil.isNotEmpty(electricityCabinetList)){
                for (ElectricityCabinet electricityCabinet:electricityCabinetList) {
                    eleIdList.add(electricityCabinet.getId());
                }
            }

        }

       return eleIdList;
    }
}
