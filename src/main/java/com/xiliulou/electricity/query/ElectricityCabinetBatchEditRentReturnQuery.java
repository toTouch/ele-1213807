package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
     * 最小是否限制 0 无限制，1限制
     */
    private Integer minIsLimit;
    
    /**
     * 最大是否限制 0 无限制，1限制
     */
    private Integer maxIsLimit;
    
    
    @Valid
    private List<ElectricityCabinetBatchEditRentReturnCountQuery> countQueryList;
    
    
    
    
    
    
    
    /**
     * 无限制，更新为null
     */
    public final static Integer NOT_LIMIT = 0;
    
    /**
     * 限制，前端传递数字
     */
    public final static Integer LIMIT = 1;
}
