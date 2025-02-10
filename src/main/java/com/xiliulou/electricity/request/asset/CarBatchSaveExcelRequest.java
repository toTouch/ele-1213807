package com.xiliulou.electricity.request.asset;

import com.xiliulou.electricity.validator.CreateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class CarBatchSaveExcelRequest {
    
    /**
     * 车辆sn
     */
    @NotBlank(message = "车辆sn不能为空!", groups = {CreateGroup.class})
    private String sn;
    
    /**
     * 车牌号
     */
    @Size(max = 10, message = "车牌号输入长度超限，最长10位，请检查!", groups = {CreateGroup.class})
    private String licensePlateNumber;
    
    /**
     * 车架号
     */
    @Size(max = 17, message = "车架号输入长度超限，最长17位，请检查!", groups = {CreateGroup.class})
    private String vin;
    
    /**
     * 电机号
     */
    @Size(max = 17, message = "电机号输入长度超限，最长17位，请检查!", groups = {CreateGroup.class})
    private String motorNumber;
    
}
