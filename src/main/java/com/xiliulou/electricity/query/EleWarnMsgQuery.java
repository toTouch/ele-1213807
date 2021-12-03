package com.xiliulou.electricity.query;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author: lxc
 * @Date: 2021/3/29 10:02
 * @Description:
 */
@Data
@Builder
public class EleWarnMsgQuery {
    private Long size;
    private Long offset;

    private Integer electricityCabinetId;

    private Integer type;
    private Integer status;

    List<Integer> eleIdList;

    private Integer tenantId;
    private Integer cellNo;
}
