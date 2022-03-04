package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: lxc
 * @Date: 2021/4/15 16:02
 * @Description:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShareMoneyActivityQuery {
    private Long size;
    private Long offset;
    private String name;

    private Integer tenantId;

    /**
     * 活动状态
     */
    private Integer status;


}
