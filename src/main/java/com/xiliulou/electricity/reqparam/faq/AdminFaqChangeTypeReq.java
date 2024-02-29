package com.xiliulou.electricity.reqparam.faq;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 常见问题添加参数
 *
 * @author kuz
 * @date 2024/2/23 16:11
 */
@Data
public class AdminFaqChangeTypeReq {
    
    
    /**
     * 切换分类
     */
    @NotNull(message = "typeId不能为空")
    private Long typeId;
    
    /**
     * 问题集合
     */
    @NotEmpty(message = "集合不能为空")
    private List<Long> ids;
    
}
