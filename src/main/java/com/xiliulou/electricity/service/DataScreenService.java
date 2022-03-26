package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;

/**
 * 数据大屏service
 *
 * @author hrp
 * @since 2022-03-22 10:56:56
 */
public interface DataScreenService {

    R queryOrderStatistics(Integer tenantId);

    R queryDataBrowsing(Integer tenantId);

    R queryMapProvince(Integer tenantId);

    R queryMapCity(Integer tenantId,Integer pid);

}
