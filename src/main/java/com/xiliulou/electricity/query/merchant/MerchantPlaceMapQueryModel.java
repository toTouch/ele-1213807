package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * @author maxiaodong
 * @date 2024/2/9 16:14
 * @desc
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MerchantPlaceMapQueryModel {
    private List<Long> placeIdList;
    private Long merchantId;
    private Integer eqFlag;
    
    private Set<Long> merchantIdList;
    public final static Integer EQ = 1;
    public final static Integer NO_EQ = 0;
}
