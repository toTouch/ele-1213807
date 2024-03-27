package com.xiliulou.electricity.vo.faq;

import com.xiliulou.electricity.enums.UpDownEnum;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FaqCategoryVo {
    
    private Long id;
    
    private String type;
    
    private BigDecimal sort;
    
    private Integer count;
    
}
