package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.electricity.entity.ElectricityCabinetBind;
import com.xiliulou.electricity.service.ElectricityCabinetBindService;
import com.xiliulou.electricity.service.UserTypeService;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("typeUserCabinetService")
public class TypeUserCabinetServiceImpl implements UserTypeService {
    @Autowired
    ElectricityCabinetBindService electricityCabinetBindService;
    @Override
    public List<Integer> getEleIdListByUserType(TokenUser user) {
        List<ElectricityCabinetBind> electricityCabinetBindList= electricityCabinetBindService.queryElectricityCabinetList(user.getUid());
        if(ObjectUtil.isEmpty(electricityCabinetBindList)){
            return null;
        }
        List<Integer> eleIdList=new ArrayList<>();
        for (ElectricityCabinetBind electricityCabinetBind:electricityCabinetBindList) {
            eleIdList.add(electricityCabinetBind.getElectricityCabinetId());
        }
        return eleIdList;
    }
}
