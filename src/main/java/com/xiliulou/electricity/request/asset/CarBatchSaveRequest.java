package com.xiliulou.electricity.request.asset;

import com.xiliulou.electricity.validator.CreateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class CarBatchSaveRequest {
    
    /**
     * 车辆sn
     */
    @NotEmpty(message = "车辆sn不能为空!", groups = {CreateGroup.class})
    private List<String> snList;
    
    /**
     * 型号Id
     */
    @NotNull(message = "厂家型号不能为空!!", groups = {CreateGroup.class})
    private Integer modelId;
}
