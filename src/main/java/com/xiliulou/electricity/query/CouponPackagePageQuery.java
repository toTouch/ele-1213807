package com.xiliulou.electricity.query;

import com.xiliulou.electricity.query.base.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author : renhang
 * @description CouponPackagePageQuery
 * @date : 2025-01-17 10:51
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class CouponPackagePageQuery extends BasePageQuery {

    private String packageName;

    private Long franchiseeId;

    private Integer inCanBuy;

    private List<Long> franchiseeIds;

    private Integer tenantId;
}
