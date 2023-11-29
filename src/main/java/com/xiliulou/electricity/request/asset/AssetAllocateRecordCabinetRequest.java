package com.xiliulou.electricity.request.asset;

import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import com.xiliulou.electricity.validator.CreateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author HeYafeng
 * @description 新增资产调拨柜机request
 * @date 2023/11/29 11:34:19
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetAllocateRecordCabinetRequest {
    
    /**
     * 换电柜id
     */
    @NotNull(message = "换电柜id不能为空!", groups = {CreateGroup.class})
    private Long id;
    
    /**
     * 换电柜sn
     */
    @NotBlank(message = "换电柜sn不能为空!", groups = {CreateGroup.class})
    private String sn;
    
    /**
     * 物联网productKey
     */
    @NotBlank(message = "换电柜productKey不能为空!", groups = {CreateGroup.class})
    private String productKey;
    
}
