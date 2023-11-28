package com.xiliulou.electricity.request.asset;

import com.xiliulou.electricity.validator.CreateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author HeYafeng
 * @description 新建退库
 * @date 2023/11/27 09:18:43
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetExitWarehouseSaveRequest {
    
    /**
     * 退库类型 (1-电柜, 2-电池, 3-车辆)
     */
    @Range(min = 1, max = 3, message = "退库类型内容不合法")
    @NotNull(message = "退库类型不能为空", groups = {CreateGroup.class})
    private Integer type;
    
    /**
     * 退库加盟商
     */
    @NotNull(message = "退库加盟商不能为空", groups = {CreateGroup.class})
    private Long franchiseeId;
    
    /**
     * 退库门店
     */
    private Long storeId;
    
    /**
     * 电池/车辆/电柜 编号
     */
    @NotEmpty(message = "退库编号不能为空", groups = {CreateGroup.class})
    private List<String> snList;
    
    /**
     * 退库库房id
     */
    @NotNull(message = "退库库房不能为空", groups = {CreateGroup.class})
    private Long warehouseId;
    
}
