package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
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
    @NotNull(message = "加盟商Id不能为空!", groups = {CreateGroup.class})
    private Integer franchiseeId;
    /**
     * 换电柜Id
     */
    private List<Long> electricityBatteryIdList;


}
