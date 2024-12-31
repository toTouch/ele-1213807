package com.xiliulou.electricity.query.exchange;

import com.xiliulou.electricity.query.base.BasePageQuery;
import lombok.Data;

import java.util.List;

/**
 * @author renhang
 */
@Data
public class ElectricityCabinetBoxLockPageQuery extends BasePageQuery {

    private String sn;

    private String name;

    private Long areaId;

    private String address;

    private Long franchiseeId;

    private Long storeId;

    private Integer lockType;

    private Long lockStatusChangeTimeStart;

    private Long lockStatusChangeTimeEnd;

    private Integer tenantId;

    private List<Long> franchiseeIds;
}
