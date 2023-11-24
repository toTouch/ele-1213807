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
     * 盘点状态
     */
    @NotNull(message = "盘点状态不能为空", groups = {UpdateGroup.class})
    private Integer status;
    
    /**
     * 盘点的电池sn码
     */
    private List<String> snList;
    
    /**
     * 操作者
     */
    private Long uid;
    
}
