package com.xiliulou.electricity.model.car.query;

import com.xiliulou.electricity.enums.RentalTypeEnum;
import com.xiliulou.electricity.enums.car.CarRentalStateEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 车辆租赁订单表，DB层查询模型
 * @author xiaohui.song
 **/
@Data
public class CarRentalOrderQryModel implements Serializable {

    private static final long serialVersionUID = -8960817730474079740L;

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
     * 类型
     * <pre>
     *     1-租借
     *     2-归还
     * </pre>
     * @see RentalTypeEnum
     */
    private Integer type;

    /**
     * 订单状态
     * <pre>
     *     1-审核中
     *     2-成功
     *     3-审核拒绝
     * </pre>
     * @see CarRentalStateEnum
     */
    private Integer rentalState;

    /**
     * 车辆SN码(模糊搜索)
     */
    private String carSn;

    /**
     * 加盟商ID集
     */
    private List<Integer> franchiseeIdList;

    /**
     * 门店ID集
     */
    private List<Integer> storeIdList;

    /**
     * 创建时间起始
     */
    private Long beginCreateTime;

    /**
     * 创建时间截止
     */
    private Long endCreateTime;
    
    /**
     * @see com.xiliulou.core.base.enums.ChannelEnum
     */
    private String channel;
}
