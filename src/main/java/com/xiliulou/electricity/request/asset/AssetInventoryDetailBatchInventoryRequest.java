package com.xiliulou.electricity.request.asset;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author HeYafeng
 * @description 批量盘点
 * @date 2023/11/20 21:33:56
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetInventoryDetailBatchInventoryRequest {
    
    /**
     * 盘点单号
     */
    @NotEmpty(message = "盘点单号不能为空", groups = {UpdateGroup.class})
    private String orderNo;
    
    /**
     * 盘点状态(0-正常,1-故障, 2-库存, 3-丢失, 4-报废)
     */
    @NotNull(message = "盘点状态不能为空", groups = {UpdateGroup.class})
    private Integer status;
    
    /**
     * 盘点的电池sn码
     */
    @NotEmpty(message = "盘点资产不能为空", groups = {UpdateGroup.class})
    private List<String> snList;
}
