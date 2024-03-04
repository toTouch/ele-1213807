package com.xiliulou.electricity.bo.faq;

import com.xiliulou.electricity.entity.faq.FaqV2;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FaqV2BO extends FaqV2 {
    private String type;
    
    private BigDecimal typeSort;
}
