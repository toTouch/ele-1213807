package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: eclair
 * @Date: 2020/6/15 10:02
 * @Description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ElectricityCabinetModelQuery {
    private Long size;
    private Long offset;
    private String name;
}
