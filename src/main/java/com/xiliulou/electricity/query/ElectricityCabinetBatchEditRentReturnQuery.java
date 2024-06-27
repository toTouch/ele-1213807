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
    
    /**
     * 租电类型（全部可租电、不允许租电、最少保留一块电池、自定义） RentReturnNormEnum
     */
    private Integer rentTabType;
    
    /**
     * 退电类型（全部可退电、不允许退电、最少保留一个空仓、自定义） RentReturnNormEnum
     */
    private Integer returnTabType;
    
    
    @Valid
    private List<ElectricityCabinetBatchEditRentReturnCountQuery> countQueryList;
    
}
