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
public class ShareActivityQuery {
    private Long size;
    private Long offset;
    private String name;
    private List<Integer> typeList;
    /**
     * 加盟商Id
     */
    private Long franchiseeId;

    private Integer tenantId;


}
