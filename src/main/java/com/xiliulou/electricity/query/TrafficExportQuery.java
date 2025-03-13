package com.xiliulou.electricity.query;


import lombok.Data;

/**
 * @author : renhang
 * @description TrafficExportQuery
 * @date : 2025-03-13 14:16
 **/
@Data
public class TrafficExportQuery {

    /**
     * 1:全部租户 2:单个租户
     */
    private Integer selectTab;

    private Integer tenantId;


    public static Integer ALONE_TENANT = 2;
}
