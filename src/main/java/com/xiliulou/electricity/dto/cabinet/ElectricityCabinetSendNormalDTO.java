package com.xiliulou.electricity.dto.cabinet;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ElectricityCabinetSendNormalDTO {
    private String sessionId;

    private String operatorId;

    private Integer cabinetId;
}
