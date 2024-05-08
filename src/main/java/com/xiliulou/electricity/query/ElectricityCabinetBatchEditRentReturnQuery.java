package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.Valid;
import java.util.List;

/**
 * @ClassName: ElectricityCabinetBatchEditRentReturnQuery
 * @description:
 * @author: renhang
 * @create: 2024-05-08 17:40
 */
@Data
public class ElectricityCabinetBatchEditRentReturnQuery {
    
    
    @Valid
    private List<ElectricityCabinetBatchEditRentReturnCountQuery> countQueryList;
    
}
