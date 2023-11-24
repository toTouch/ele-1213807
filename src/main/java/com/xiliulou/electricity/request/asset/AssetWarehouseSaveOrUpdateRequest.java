package com.xiliulou.electricity.request.asset;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
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
    @NotNull(message = "库房ID不能为空!", groups = {UpdateGroup.class})
    private Long id;
    
    /**
     * 库房名称
     */
    @NotBlank(message = "库房名称不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    @Length(min = 1, max = 50, message = "名称不合法", groups = {CreateGroup.class, UpdateGroup.class})
    private String name;
    
    /**
     * 库房地址
     */
    @Length(min = 1, max = 100, message = "库房地址不合法", groups = {CreateGroup.class, UpdateGroup.class})
    private String address;
    
    /**
     * 库房状态
     */
    @NotNull(message = "库房状态不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer status;
    
    /**
     * 库房管理员
     */
    @Length(min = 1, max = 50, message = "名称不合法", groups = {CreateGroup.class, UpdateGroup.class})
    private String managerName;
    
    /**
     * 联系方式
     */
    @Length(min = 1, max = 50, message = "名称不合法", groups = {CreateGroup.class, UpdateGroup.class})
    private String managerPhone;
    
    private Long uid;
}
