package com.xiliulou.electricity.model.car.query;

import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 租车套餐订单逾期表，DB层查询模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageOrderSlippageQryModel implements Serializable {

    private static final long serialVersionUID = 3123132929941469801L;

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
     * 套餐ID
     */
    private Long rentalPackageId;

    /**
     * 套餐类型
     * <pre>
     *     1-单车
     *     2-车电一体
     * </pre>
     * @see RentalPackageTypeEnum
     */
    private Integer rentalPackageType;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 支付状态
     * <pre>
     *     1-未支付
     *     2-支付成功
     *     3-支付失败
     *     4-取消支付
     *     5-已清除
     * </pre>
     * @see PayStateEnum
     */
    private Integer payState;

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

    /**
     * 支付状态
     * <pre>
     *     1-未支付
     *     2-支付成功
     *     3-支付失败
     *     4-取消支付
     *     5-已清除
     * </pre>
     * @see PayStateEnum
     */
    private List<Integer> payStateList;

    /**
     * 支付时间开始
     */
    private Long payTimeBegin;

    /**
     * 支付时间截止
     */
    private Long payTimeEnd;
    
    /**
     * 根据计费开始时间排序，1-正序，2-倒叙
     */
    private Integer orderByLateFeeStartTime;
    
    /**
     * 根据支付时间排序，1-正序，2-倒叙
     */
    private Integer orderByPayTime;
}
