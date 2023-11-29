package com.xiliulou.electricity.request.asset;

import com.xiliulou.electricity.enums.asset.AssetTypeEnum;
import com.xiliulou.electricity.validator.CreateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author HeYafeng
 * @description 新增资产调拨request
 * @date 2023/11/29 11:34:19
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetAllocateRecordRequest {
    
    /**
     * 调拨资产类型 (1-电柜, 2-电池, 3-车辆)
     *
     * @see AssetTypeEnum
     */
    @Range(min = 1, max = 3, message = "调拨类型不合法")
    @NotNull(message = "调拨类型不能为空", groups = {CreateGroup.class})
    private Integer type;
    
    /**
     * 调出加盟商
     */
    @NotNull(message = "调出加盟商不能为空", groups = {CreateGroup.class})
    private Long sourceFranchiseeId;
    
    /**
     * 调入加盟商
     */
    @NotNull(message = "调入加盟商不能为空", groups = {CreateGroup.class})
    private Long targetFranchiseeId;
    
    /**
     * 调出门店
     */
    private Long sourceStoreId;
    
    /**
     * 调入门店
     */
    private Long targetStoreId;
    
    /**
     * 调拨原因
     */
    @Size(max = 350, message = "调拨原因输入过长")
    private String remark;
    
    /**
     * 调拨资产sn列表
     */
    private List<Long> idList;
    
    /**
     * 换电柜资产列表
     */
    private List<AssetAllocateRecordCabinetRequest> cabinetList;
    
}
