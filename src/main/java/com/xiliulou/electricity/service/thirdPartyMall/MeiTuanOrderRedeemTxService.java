package com.xiliulou.electricity.service.thirdPartyMall;

import com.xiliulou.electricity.bo.meituan.MeiTuanOrderRedeemRollBackBO;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallOrder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * @author HeYafeng
 * @description
 * @date 2024/9/13 08:59:14
 */
public interface MeiTuanOrderRedeemTxService {
    
    Pair<ElectricityMemberCardOrder, MeiTuanOrderRedeemRollBackBO> saveUserInfoAndOrder(UserInfo userInfo, BatteryMemberCard batteryMemberCard,
            UserBatteryMemberCard userBatteryMemberCard, MeiTuanRiderMallOrder meiTuanRiderMallOrder);
    
    Pair<ElectricityMemberCardOrder, MeiTuanOrderRedeemRollBackBO> bindUserMemberCard(UserInfo userInfo, BatteryMemberCard batteryMemberCard,
            MeiTuanRiderMallOrder meiTuanRiderMallOrder);
    
    Pair<ElectricityMemberCardOrder, MeiTuanOrderRedeemRollBackBO> saveRenewalUserBatteryMemberCardOrder(UserInfo userInfo, BatteryMemberCard batteryMemberCard,
            UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard userBindBatteryMemberCard, MeiTuanRiderMallOrder meiTuanRiderMallOrder,
            List<String> userBindBatteryTypes, List<String> memberCardBatteryTypes);
    
    void rollback(MeiTuanOrderRedeemRollBackBO rollBackBO);
}
