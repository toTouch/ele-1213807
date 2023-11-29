package com.xiliulou.electricity.request.asset;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 *
 * @author zhangyongbo
 * @since 2023-11-29 11:00:14
 */
@Data
public class CarAddRequest {
    /**
     * 车辆sn
     */
    @NotEmpty(message = "车辆sn不能为空!", groups = {CreateGroup.class})
    private String sn;
    
    /**
     * 型号Id
     */
    @NotNull(message = "厂家型号不能为空!!", groups = {CreateGroup.class})
    private Integer modelId;
    
    /**
     * 库房id
     */
    private Long warehouseId;
    
}
