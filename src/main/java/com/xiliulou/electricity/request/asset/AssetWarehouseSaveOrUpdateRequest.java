package com.xiliulou.electricity.request.asset;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author HeYafeng
 * @description 新增库房
 * @date 2023/11/21 15:42:11
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetWarehouseSaveOrUpdateRequest {
    /**
     * 库房ID
     */
    private Long id;
    
    /**
     * 库房名称
     */
    @NotEmpty(message = "库房名称不能为空!", groups = {CreateGroup.class})
    private String name;
    
    /**
     * 库房地址
     */
    private String address;
    
    /**
     * 库房状态
     */
    @NotNull(message = "库房状态不能为空!", groups = {UpdateGroup.class})
    private Integer status;
    
    /**
     * 库房管理员
     */
    private String managerName;
    
    /**
     * 联系方式
     */
    private String managerPhone;
    
    private Long uid;
}
