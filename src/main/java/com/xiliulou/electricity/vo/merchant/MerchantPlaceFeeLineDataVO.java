package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/25 21:11
 * @desc
 */
@Data
public class MerchantPlaceFeeLineDataVO {
    private List<String> xDataList;
    private List<BigDecimal> yDataList;
    
}
