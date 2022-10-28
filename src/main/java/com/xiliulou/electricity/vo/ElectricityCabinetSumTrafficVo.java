package com.xiliulou.electricity.vo;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class ElectricityCabinetSumTrafficVo {
    private BigDecimal sumTraffic;

    private List<ElectricityCabinetTrafficVo> electricityCabinetTrafficVos;
}

