package com.xiliulou.electricity.request.asset;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 *
 * @author zhangyongbo
 * @since 2023-11-29 11:00:14
 */
@Data
public class CarOutWarehouseRequest {
    
    /**
     * 车辆id
     */
    @NotEmpty(message = "出库车辆id不能为空", groups = {UpdateGroup.class})
    private List<Integer> idList;
    
    /**
     * 加盟商id
     */
    @NotNull(message = "加盟商id不能为空", groups = {UpdateGroup.class})
    private Long franchiseeId;
    
    /**
     * 门店id
     */
    @NotNull(message = "门店id不能为空", groups = {UpdateGroup.class})
    private Integer storeId;
}
