package com.xiliulou.electricity.request.asset;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author HeYafeng
 * @description 新增和更新资产盘点
 * @date 2023/11/20 13:47:42
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetInventorySaveOrUpdateRequest {
    
    /**
     * @description 资产盘点ID
     */
    @NotNull(message = " 资产盘点ID不能为空!", groups = {UpdateGroup.class})
    private Long id;
    
    @NotNull(message = " 盘点加盟商不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private Long franchiseeId;
    
    @NotNull(message = "盘点结束时间不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private Long finishTime;
    
    /**
     * 操作人uid
     */
    private Long uid;
    
}
