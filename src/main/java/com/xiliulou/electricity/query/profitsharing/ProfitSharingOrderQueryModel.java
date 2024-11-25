/**
 * Create date: 2024/8/29
 */

package com.xiliulou.electricity.query.profitsharing;

import lombok.Data;

import java.util.List;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/29 18:04
 */
@Data
public class ProfitSharingOrderQueryModel {
    
    private Long startId;
    
    private List<Integer> statusList;
    
    private Integer tenantId;
    
    private Integer size;
    
    private Integer type;
}
