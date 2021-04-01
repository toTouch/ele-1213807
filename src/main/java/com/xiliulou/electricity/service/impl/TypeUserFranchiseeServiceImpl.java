package com.xiliulou.electricity.service.impl;
import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeBind;
import com.xiliulou.electricity.entity.StoreBindElectricityCabinet;
import com.xiliulou.electricity.service.FranchiseeBindService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreBindElectricityCabinetService;
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
    FranchiseeBindService franchiseeBindService;
    @Autowired
    StoreBindElectricityCabinetService storeBindElectricityCabinetService;
    @Override
    public List<Integer> getEleIdListByUserType(TokenUser user) {
        //1、先找到加盟商用户中的加盟商
        List<Franchisee> franchiseeList=franchiseeService.queryByUid(user.getUid());
        if(ObjectUtil.isEmpty(franchiseeList)){
            return null;
        }
        List<FranchiseeBind> franchiseeBinds=new ArrayList<>();
        for (Franchisee franchisee:franchiseeList) {
            List<FranchiseeBind> franchiseeBindList= franchiseeBindService.queryByFranchiseeId(franchisee.getId());
            franchiseeBinds.addAll(franchiseeBindList);
        }
        if(ObjectUtil.isEmpty(franchiseeBinds)){
            return null;
        }
        //2、再找加盟商绑定的门店
        List<Integer> storeIdList=new ArrayList<>();
        for (FranchiseeBind franchiseeBind:franchiseeBinds) {
            storeIdList.add(franchiseeBind.getStoreId());
        }
        if(ObjectUtil.isEmpty(storeIdList)){
            return null;
        }
        //3、再找门店绑定的柜子
        List<StoreBindElectricityCabinet> storeBindElectricityCabinets=new ArrayList<>();
        for (Integer storeId:storeIdList) {
            List<StoreBindElectricityCabinet> storeBindElectricityCabinetList=storeBindElectricityCabinetService.queryByStoreId(storeId);
            storeBindElectricityCabinets.addAll(storeBindElectricityCabinetList);
        }
        if(ObjectUtil.isEmpty(storeBindElectricityCabinets)){
            return null;
        }
        List<Integer> eleIdList=new ArrayList<>();
        for (StoreBindElectricityCabinet storeBindElectricityCabinet:storeBindElectricityCabinets) {
            eleIdList.add(storeBindElectricityCabinet.getElectricityCabinetId());
        }
       return eleIdList;
    }
}
