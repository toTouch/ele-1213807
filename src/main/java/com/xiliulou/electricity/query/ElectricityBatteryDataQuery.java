package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ElectricityBatteryDataQuery {

    /**
     * sn码
     */
    private String sn;
    /**
     * 电池物理状态 0：在仓，1：不在仓
     */
    private Integer physicsStatus;
    /**
     * 电池业务状态：1：已录入，2：租借，3：归还，4：异常交换
     */
    private Integer businessStatus;



    private Integer tenantId;


    private Long franchiseeId;
    

    private Integer electricityCabinetId;
    /**
     * 1.全部电池   2. 在柜电池     3. 待租电池     4. 已租电池     5. 游离电池     6. 逾期电池
     */
    private Integer queryType;
    /**
     * 全部电池
     */
    public static final Integer QUERY_TYPE_ALL=1;

    /**
     * 在柜电池
     */
    public static final Integer QUERY_TYPE_INCABINET=2;

    /**
     * 待租电池
     */
    public static final Integer QUERY_TYPE_PENDINGRENTAL=3;

    /**
     * 已租电池
     */
    public static final Integer QUERY_TYPE_LEASED=4;

    /**
     * 游离电池
     */
    public static final Integer QUERY_TYPE_STRAY=5;

    /**
     * 逾期电池
     */
    public static final Integer QUERY_TYPE_OVERDUE=6;


    /**
     * 所属用户id
     */
    private Long uid;
    /**
     * 当前时间
     */
    private Long currentTimeMillis;

    private List<Long> franchiseeIds;
}
