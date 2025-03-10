package com.xiliulou.electricity.request.cabinet;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@Data
public class ElectricityCabinetServerUpdateRequest {
    /**
     * 租户id
     */
    private Integer tenantId;

    /**
     * 延长期限：单位年
     */
    @NotNull(message = "[延长期限]不能为空")
    private Integer yearNum;

    private Set<String> cabinetSnList;
}
