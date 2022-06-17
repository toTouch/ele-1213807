package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.FranchiseeUserInfo;
import com.xiliulou.electricity.vo.EleBatteryServiceFeeVO;
import com.xiliulou.electricity.vo.ElectricityBatteryVO;

import java.util.List;

/**
 * 用户绑定列表(FranchiseeUserInfo)表服务接口
 *
 * @author makejava
 * @since 2021-06-17 10:10:13
 */
public interface FranchiseeUserInfoService {

    /**
     * 修改数据
     *
     * @param franchiseeUserInfo 实例对象
     * @return 实例对象
     */
    Integer update(FranchiseeUserInfo franchiseeUserInfo);

    FranchiseeUserInfo queryByUserInfoId(Long id);

    /*Integer queryCountByBatterySn(String electricityBatterySn);*/

    Integer unBind(FranchiseeUserInfo franchiseeUserInfo);

    Integer minCount(Long id);

    Integer minCountForOffLineEle(Long id);

	Integer plusCount(Long id);

    void updateByUserInfoId(FranchiseeUserInfo franchiseeUserInfo);

	void updateRefund(FranchiseeUserInfo franchiseeUserInfo);

	FranchiseeUserInfo insert(FranchiseeUserInfo insertFranchiseeUserInfo);

	Integer queryCountByFranchiseeId(Long id);

    R updateBattery(String batteryType);

    R queryBattery();

    Integer deleteByUserInfoId(Long userInfoId);

	void updateByOrder(FranchiseeUserInfo franchiseeUserInfo);

	void updateOrderByUserInfoId(FranchiseeUserInfo franchiseeUserInfo);

    EleBatteryServiceFeeVO queryUserBatteryServiceFee(Long uid);

    void updateRentCar(FranchiseeUserInfo franchiseeUserInfo);
}
