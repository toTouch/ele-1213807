package com.xiliulou.electricity.mapper.enterprise;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.enterprise.CloudBeanUseRecord;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/23 8:36
 */
public interface EnterpriseBatteryPackageMapper extends BaseMapper<CloudBeanUseRecord> {

    Integer insertMemberCardOrder(ElectricityMemberCardOrder memberCardOrder);


}
