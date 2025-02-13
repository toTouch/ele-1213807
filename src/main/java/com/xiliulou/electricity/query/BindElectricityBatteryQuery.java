package com.xiliulou.electricity.query;

import com.xiliulou.electricity.enums.asset.WarehouseOperateTypeEnum;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 门店表(TStore)实体类
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@Data
public class BindElectricityBatteryQuery {
    /**
    * 加盟商Id
    */
    private Integer franchiseeId;
    /**
     * 电池Id
     */
    private List<Long> electricityBatteryIdList;
    
    /**
     * 电池sn
     */
    private List<String> electricityBatterySnList;

    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 操作类型
     * @see WarehouseOperateTypeEnum
     */
    private Integer type;
}
