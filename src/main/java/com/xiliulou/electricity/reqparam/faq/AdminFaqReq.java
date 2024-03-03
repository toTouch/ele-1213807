package com.xiliulou.electricity.reqparam.faq;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 常见问题添加参数
 *
 * @author kuz
 * @date 2024/2/23 16:11
 */
@Data
public class AdminFaqReq {
    
  
    
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Long id;
    
    /**
     * 分类id
     */
    @NotNull(message = "typeId不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Long typeId;
    
    /**
     * 标题
     */
    @NotBlank(message = "title不能为空",groups = {CreateGroup.class, UpdateGroup.class})
    @Size(min = 1, max = 100, message = "title长度在1-100之间", groups = {CreateGroup.class, UpdateGroup.class})
    private String title;
    
    /**
     * 答案
     */
    @NotBlank(message = "answer不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String answer;
    
    /**
     * 排序
     */
    @NotNull(message = "sort不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private BigDecimal sort;
}
