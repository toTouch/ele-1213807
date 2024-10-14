package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

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
    @Length(min = 1, max = 32, message = "参数不合法")
    private String deviceName;
    
    private String remark;
}
