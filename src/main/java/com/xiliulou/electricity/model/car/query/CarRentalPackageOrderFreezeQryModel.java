package com.xiliulou.electricity.model.car.query;

import com.xiliulou.electricity.enums.RentalPackageOrderFreezeStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 租车套餐订单冻结表，DB层查询模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageOrderFreezeQryModel implements Serializable {

    private static final long serialVersionUID = 5816010520362253658L;

    /**
     * 偏移量
     */
    private Integer offset = 0;

    /**
     * 取值数量
     */
    private Integer size = 10;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 加盟商ID
     */
    private Integer franchiseeId;

    /**
     * 门店ID
     */
    private Integer storeId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 状态
     * <pre>
     *     1-待审核
     *     2-审核通过
     *     3-审核拒绝
     *     4-提前启用
     *     5-自动启用
     *     6-撤回申请
     *     7-已失效
     * </pre>
     * @see RentalPackageOrderFreezeStatusEnum
     */
    private Integer status;

    /**
     * 创建时间开始
     */
    private Long createTimeBegin;

    /**
     * 创建时间截止
     */
    private Long createTimeEnd;

    /**
     * 加盟商ID集
     */
    private List<Integer> franchiseeIdList;

    /**
     * 门店ID集
     */
    private List<Integer> storeIdList;
}
