package com.xiliulou.electricity.dto;

import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.entity.ElectricityCarModel;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author HeYafeng
 * @date 2025/1/16 10:03:40
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CarUserMemberInfoProDTO {
    
    private List<CarRentalPackageMemberTermPo> memberTermList;
    
    private Map<Long, CarRentalPackageMemberTermPo> userMemberTermMap;
    
    private Map<String, CarRentalPackageOrderPo> userUsingPackageOrderMap;
    
    private Map<Long, CarRentalPackagePo> userUsingCarPackageMap;
    
    private Map<String, CarRentalPackageDepositPayPo> userUsingDepositMap;
    
    private Map<Long, ElectricityCarModel> usingPackageCarModelMap;
    
    private Map<Long, ElectricityCar> userCarMap;
    
    private Map<Long, BigDecimal> userLateFeeMap;
    
}
