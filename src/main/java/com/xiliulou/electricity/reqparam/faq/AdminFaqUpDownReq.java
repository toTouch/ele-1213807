package com.xiliulou.electricity.reqparam.faq;

import com.xiliulou.electricity.enums.UpDownEnum;
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
public class AdminFaqUpDownReq {
    
    
    /**
     * 上下架状态 'UP表示上架  DOWN表示下架'
     */
    @NotNull(message = "上下架状态不能为空")
    private UpDownEnum onShelf;
    
    /**
     * 问题集合
     */
    @NotEmpty(message = "集合不能为空")
    private List<Long> ids;
    
}
