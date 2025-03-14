package com.xiliulou.electricity.service.battery;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.battery.ElectricityBatteryLabel;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.request.battery.BatteryLabelBatchUpdateRequest;
import com.xiliulou.electricity.vo.ElectricityBatteryDataVO;
import com.xiliulou.electricity.vo.battery.ElectricityBatteryLabelVO;

import java.util.List;

/**
 * @author: SJP
 * @Desc:
 * @create: 2025-02-20 17:54
 **/
public interface ElectricityBatteryLabelBizService {
    
    R updateRemark(String sn, String remark);
    
    /**
     * 使用此方法时需要注意batteryLabel内设置的属性，只设置自己需要保存或需要更新的属性，后续代码会自动根据对象内的非null属性处理逻辑，设置了多余的属性会出现意料之外的数据修改
     */
    void updateOrInsertBatteryLabel(ElectricityBattery battery, ElectricityBatteryLabel batteryLabel);
    
    R batchUpdate(BatteryLabelBatchUpdateRequest request);
    
    boolean permissionVerificationForReceiver(ElectricityBattery battery, Long receiverId, Integer label, ElectricityBatteryLabel batteryLabel);
    
    List<ElectricityBatteryLabelVO> listLabelVOByBatteries(List<String> sns, List<ElectricityBattery> electricityBatteryList);
    
    List<ElectricityBatteryLabelVO> listLabelVOByDataVOs(List<String> sns, List<ElectricityBatteryDataVO> electricityBatteries);
    
    /**
     * 校验电池租赁状态与套餐到期时间，获取当前对应的租借标签
     * 根据UserBatteryMemberCard与CarRentalPackageMemberTermPo判断是电套餐还是车电一体，传递参数需注意
     */
    void checkRentStatusForLabel(UserBatteryMemberCard userBatteryMemberCard, CarRentalPackageMemberTermPo memberTermPo);
}
