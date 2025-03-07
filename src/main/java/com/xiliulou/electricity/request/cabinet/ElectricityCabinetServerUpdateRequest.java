package com.xiliulou.electricity.request.cabinet;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ElectricityCabinetServerUpdateRequest {
    /**
     * 租户id
     */
    @NotNull(message = "[租户id]不能为空")
    private Integer tenantId;

    /**
     * 延长期限：单位年
     */
    @NotNull(message = "[延长期限]不能为空")
    private Integer yearNum;

    private List<String> cabinetSnList;
}
