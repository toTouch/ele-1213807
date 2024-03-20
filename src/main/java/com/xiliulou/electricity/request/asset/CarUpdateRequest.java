package com.xiliulou.electricity.request.asset;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class CarUpdateRequest {
    
    /**
     * 车辆Id
     */
    @NotNull(message = "车辆Id不能为空!", groups = {UpdateGroup.class})
    private Long id;
    
    /**
     * 车牌号
     */
    @Size(max = 9, message = "车牌号输入有误!", groups = {UpdateGroup.class})
    private String licensePlateNumber;
    
    /**
     * 车架号
     */
    @Size(max = 17, message = "车架号输入有误!", groups = {UpdateGroup.class})
    private String vin;
    
    /**
     * 电机号
     */
    @Size(max = 17, message = "电机号输入有误!", groups = {UpdateGroup.class})
    private String motorNumber;
    
}
