package com.xiliulou.electricity.query;
import lombok.Builder;
import lombok.Data;

/**
 * @author: eclair
 * @Date: 2020/6/15 10:02
 * @Description:
 */
@Data
@Builder
public class EleWarnMsgQuery {
    private Long size;
    private Long offset;

    private Integer electricityCabinetId;

    private Integer type;
}
