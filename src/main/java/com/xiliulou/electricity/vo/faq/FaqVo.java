package com.xiliulou.electricity.vo.faq;

import com.xiliulou.electricity.enums.UpDownEnum;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FaqVo {
    
    private Long id;
    
    private String title;
    
    private String answer;
    
    private Long typeId;
    
    private Integer onShelf;
    
    private BigDecimal sort;
    
    private String type;
    
    private BigDecimal typeSort;
    
    private Integer tenantId;
    
    private Long opUser;
    
    private Long createTime;
    
    private Long updateTime;
}
