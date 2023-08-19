package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.entity.EleBatteryServiceFeeOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-08-18-20:51
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserServiceFeeDetail {

    private EleBatteryServiceFeeOrder expireBatteryServiceFeeOrder;

    private EleBatteryServiceFeeOrder disableBatteryServiceFeeOrder;
}
