package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;


/**
 * @author : eclair
 * @date : 2021/9/26 5:02 下午
 */
@Data
@Builder
public class NotExistSnQuery {
    private Integer size;
    private Integer offset;

    private Integer eId;
}
