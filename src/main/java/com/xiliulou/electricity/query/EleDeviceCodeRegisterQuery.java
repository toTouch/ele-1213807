package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EleDeviceCodeRegisterQuery {
    @Valid
    @NotEmpty(message = "deviceName不能为空", groups = CreateGroup.class)
    @Size(min = 1, max = 500, message = "参数不合法")
    private Set<EleDeviceCodeOuterQuery> deviceNames;
    
    @NotEmpty(message = "deviceName不能为空", groups = UpdateGroup.class)
    @Size(min = 1, max = 500, message = "参数不合法")
    private Set<String> deviceNameSet;
    
    @NotEmpty(message = "productKey不能为空", groups = UpdateGroup.class)
    private String productKey;
}
