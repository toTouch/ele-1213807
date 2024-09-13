package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FranchiseeIdNameVO {

    /**
     * Id
     */
    private Long id;

    /**
     * 门店名称
     */
    private String name;
}
