package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeUserInfo;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.FranchiseeUserInfoMapper;
import com.xiliulou.electricity.query.ModelBatteryDeposit;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.FranchiseeUserInfoService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.vo.EleBatteryServiceFeeVO;
import com.xiliulou.electricity.vo.ElectricityBatteryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
/**
 * 用户绑定列表(FranchiseeUserInfo)表服务实现类
 *
 * @author makejava
 * @since 2021-06-17 10:10:13
 */
@Service("franchiseeUserInfoService")
@Slf4j
public class FranchiseeUserInfoServiceImpl implements FranchiseeUserInfoService {
    @Resource
    private FranchiseeUserInfoMapper franchiseeUserInfoMapper;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    ElectricityBatteryService electricityBatteryService;

    /**
     * 修改数据
     *
     * @param franchiseeUserInfo 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(FranchiseeUserInfo franchiseeUserInfo) {
       return this.franchiseeUserInfoMapper.updateById(franchiseeUserInfo);

    }

    @Override
    public FranchiseeUserInfo queryByUserInfoId(Long id) {
        return franchiseeUserInfoMapper.selectOne(new LambdaQueryWrapper<FranchiseeUserInfo>()
        .eq(FranchiseeUserInfo::getUserInfoId,id).eq(FranchiseeUserInfo::getDelFlag,FranchiseeUserInfo.DEL_NORMAL));
    }

   /* @Override
    public Integer queryCountByBatterySn(String electricityBatterySn) {
        return franchiseeUserInfoMapper.selectCount(new LambdaQueryWrapper<FranchiseeUserInfo>()
                .eq(FranchiseeUserInfo::getNowElectricityBatterySn,electricityBatterySn).eq(FranchiseeUserInfo::getDelFlag,FranchiseeUserInfo.DEL_NORMAL));
    }*/

    @Override
    public Integer unBind(FranchiseeUserInfo franchiseeUserInfo) {
        return franchiseeUserInfoMapper.unBind(franchiseeUserInfo);
    }

    @Override
    public Integer minCount(Long id) {
        return franchiseeUserInfoMapper.minCount(id);
    }

    @Override
    public Integer minCountForOffLineEle(Long id) {
        return franchiseeUserInfoMapper.minMemberCountForOffLineEle(id);
    }

    @Override
    public Integer plusCount(Long id) {
        return franchiseeUserInfoMapper.plusCount(id);
    }


    @Override
    public void updateByUserInfoId(FranchiseeUserInfo franchiseeUserInfo) {
        franchiseeUserInfoMapper.updateByUserInfoId(franchiseeUserInfo);
    }

    @Override
    public void updateRefund(FranchiseeUserInfo franchiseeUserInfo) {
        franchiseeUserInfoMapper.updateRefund(franchiseeUserInfo);
    }

    @Override
    public FranchiseeUserInfo insert(FranchiseeUserInfo insertFranchiseeUserInfo) {
        franchiseeUserInfoMapper.insert(insertFranchiseeUserInfo);
        return insertFranchiseeUserInfo;
    }

    @Override
    public Integer queryCountByFranchiseeId(Long id) {
        return franchiseeUserInfoMapper.selectCount(new LambdaQueryWrapper<FranchiseeUserInfo>().eq(FranchiseeUserInfo::getFranchiseeId,id).last("limit 0,1"));
    }

    @Override
    public void updateByOrder(FranchiseeUserInfo franchiseeUserInfo) {
        franchiseeUserInfoMapper.updateByOrder(franchiseeUserInfo);
    }

    @Override
    public void updateOrderByUserInfoId(FranchiseeUserInfo franchiseeUserInfo) {
        franchiseeUserInfoMapper.updateOrderByUserInfoId(franchiseeUserInfo);
    }

    @Override
    public EleBatteryServiceFeeVO queryUserBatteryServiceFee(Long uid) {
        //获取改用户所绑定的加盟商的电池服务费
        Franchisee franchisee=franchiseeService.queryByUserId(uid);
        //计算用户所产生的电池服务费

        EleBatteryServiceFeeVO eleBatteryServiceFeeVO=new EleBatteryServiceFeeVO();
        if (Objects.equals(franchisee.getBatteryServiceFee(),new BigDecimal(0.00))){
            return eleBatteryServiceFeeVO;
        }

        eleBatteryServiceFeeVO.setBatteryServiceFee(franchisee.getBatteryServiceFee());

        FranchiseeUserInfo franchiseeUserInfo=franchiseeUserInfoMapper.queryFranchiseeUserInfoByUid(uid);
        Long now = System.currentTimeMillis();
        if (Objects.nonNull(franchiseeUserInfo.getMemberCardExpireTime())) {
            long cardDays = (now - franchiseeUserInfo.getMemberCardExpireTime()) / 1000 / 60 / 60 / 24;
            if (Objects.nonNull(franchiseeUserInfo.getNowElectricityBatterySn()) && cardDays > 1 && Objects.equals(franchiseeUserInfo.getBatteryServiceFeeStatus(), FranchiseeUserInfo.STATUS_NOT_IS_SERVICE_FEE)) {
                //查询用户是否存在电池服务费
                Integer modelType = franchisee.getModelType();
                if (Objects.equals(modelType, Franchisee.MEW_MODEL_TYPE)) {
                    //查询用户绑定的电池类型
                    ElectricityBattery electricityBattery = electricityBatteryService.queryByBindSn(franchiseeUserInfo.getNowElectricityBatterySn());
                    String model = electricityBattery.getModel();


                    System.out.println("多型号电池服务费json数据======================="+franchisee.getModelBatteryDeposit());


                    List<ModelBatteryDeposit> modelBatteryDepositList = JsonUtil.fromJson(franchisee.getModelBatteryDeposit(), List.class);
                    eleBatteryServiceFeeVO.setModelBatteryServiceFeeList(modelBatteryDepositList);
                    for (ModelBatteryDeposit modelBatteryDeposit : modelBatteryDepositList) {
                        if (Objects.equals(model, modelBatteryDeposit.getModel())) {
                            //计算服务费
                            BigDecimal batteryServiceFee = modelBatteryDeposit.getBatteryServiceFee().multiply(new BigDecimal(cardDays));
                            eleBatteryServiceFeeVO.setUserBatteryServiceFee(batteryServiceFee);
                            return eleBatteryServiceFeeVO;
                        }
                    }
                } else {
                    BigDecimal franchiseeBatteryServiceFee = franchisee.getBatteryServiceFee();
                    //计算服务费
                    BigDecimal batteryServiceFee = franchiseeBatteryServiceFee.multiply(new BigDecimal(cardDays));
                    eleBatteryServiceFeeVO.setUserBatteryServiceFee(batteryServiceFee);
                    return eleBatteryServiceFeeVO;
                }
            }
        }

        return eleBatteryServiceFeeVO;
    }


}
