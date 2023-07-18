package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

/**
 * @author: Kenneth
 * @Date: 2023/7/10 20:29
 * @Description:
 */

@Data
@Builder
public class EleUserEsignRecordQuery {

    private Long id;

    private Long uid;

    private Long tenantId;

    private String signFlowId;

    private Integer signFinishStatus;

    private Integer delFlag;

    private Long createTime;

    private Long updateTime;

    private String name;

    private String phone;

    private Long size;

    private Long offset;


}
