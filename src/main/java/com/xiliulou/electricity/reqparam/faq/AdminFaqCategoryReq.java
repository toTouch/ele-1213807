package com.xiliulou.electricity.reqparam.faq;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 常见问题分类添加参数
 *
 * @author kuz
 * @date 2024/2/23 16:11
 */
@Data
public class AdminFaqCategoryReq {
    
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Long id;
    /**
     * 分类
     */
    @NotBlank(message = "type不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String type;
    
    /**
     * 排序
     */
    @NotNull(message = "sort不能为空", groups = {UpdateGroup.class})
    private BigDecimal sort;
}
