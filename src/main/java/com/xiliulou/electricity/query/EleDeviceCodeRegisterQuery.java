package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
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
    @NotEmpty(message = "参数不合法", groups = CreateGroup.class)
    @Size(min = 1, max = 500, message = "参数不合法")
    private Set<EleDeviceCodeOuterQuery> deviceNames;
}
