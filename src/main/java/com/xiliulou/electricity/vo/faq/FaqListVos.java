package com.xiliulou.electricity.vo.faq;

import com.xiliulou.electricity.bo.faq.FaqV2BO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FaqListVos {
    
    private Long id;
    
    private Long typeId;
    
    private List<FaqV2BO> faqBOList;
    /**
     * 分类
     */
    private String type;
    
    /**
     * 排序
     */
    private BigDecimal sort;
    
    private Integer count;
}
