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
    
    /**
     * 渠道员ID
     */
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Long id;
    
    /**
     * 渠道员UID
     */
    private Long uid;
    
    /**
     * 渠道员姓名
     */
    @NotEmpty(message = "姓名不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private String name;
    
    /**
     * 渠道员电话
     */
    @NotNull(message = "联系方式不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String phone;
    
    /**
     * 加盟商ID
     */
    @NotNull(message = "加盟商不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Long franchiseeId;
    
    /**
     * 区域ID
     */
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
