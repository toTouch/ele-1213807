package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-08-13-21:05
 */
@Data
public class EleCabinetPatternQuery {
    
    /**
     * 柜机模式：0--iot,1-tcp
     */
    @NotNull(message = "柜机模式不能为空")
    private Integer pattern;
    
    @NotBlank(message = "柜机productKey不能为空")
    private String productKey;
    
    @NotBlank(message = "柜机deviceName不能为空")
    private String deviceName;
    
    @NotBlank(message = "柜机id不能为空")
    private Integer id;
}
