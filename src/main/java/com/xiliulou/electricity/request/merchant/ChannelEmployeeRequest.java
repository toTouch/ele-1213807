package com.xiliulou.electricity.request.merchant;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author BaoYu
 * @description:
 * @date 2024/1/31 16:02
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChannelEmployeeRequest {
    
    private Long id;
    
    private Long uid;
    
    @NotEmpty(message = "姓名不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private String name;
    
    @NotNull(message = "联系方式不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String phone;
    
    @NotNull(message = "加盟商不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Long franchiseeId;
    
    private Long areaId;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 偏移量
     */
    private Integer offset;
    
    /**
     * 取值数量
     */
    private Integer size;
    
    private List<Long> idList;
    
}
