package com.xiliulou.electricity.request.asset;

import lombok.Data;

import java.util.List;

@Data
public class CarBatchSaveRequest {
    
    /**
     * 车辆sn
     */
    private List<CarBatchSaveExcelRequest> carList;
    
    /**
     * 型号Id
     */
    private Integer modelId;
}
