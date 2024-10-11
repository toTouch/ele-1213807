package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author zzlong
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EleDeviceCodeInsertQuery {
    @NotBlank(message = "deviceName不能为空")
    private String deviceName;
    
    private String remark;
}
