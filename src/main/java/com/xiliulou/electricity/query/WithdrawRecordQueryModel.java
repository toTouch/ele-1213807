package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author: maxiaodong
 * @Date: 2023/12/1 09:33
 * @Description:
 */
@Data
@Builder
public class WithdrawRecordQueryModel {
    
    private Integer tenantId;
    
    private List<Integer> idList;
}
