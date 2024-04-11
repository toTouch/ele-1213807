package com.xiliulou.electricity.entity.car;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.entity.basic.BasicCarPo;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 租车套餐购买订单表
 *
 * @author xiaohui.song
 **/
@Data
@Slf4j
@TableName("t_car_rental_package_order")
public class CarRentalPackageOrderPo extends BasicCarPo {
    
    private static final long serialVersionUID = -2568173202959559791L;
    
    /**
     * 用户ID
     */
    private Long uid;
    
    /**
     * 订单编码
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
     *
     * @see RentalPackageTypeEnum
     */
    private Integer rentalPackageType;
    
    /**
     * 套餐限制
     * <pre>
     *     0-不限制
     *     1-次数
     * </pre>
     *
     * @see RenalPackageConfineEnum
     */
    private Integer confine;
    
    /**
     * 限制数量
     */
    private Long confineNum;
    
    /**
     * 租期
     */
    private Integer tenancy;
    
    /**
     * 租期单位
     * <pre>
     *     1-天
     *     0-分钟
     * </pre>
     *
     * @see RentalUnitEnum
     */
    private Integer tenancyUnit;
    
    /**
     * 租金单价
     */
    private BigDecimal rentUnitPrice;
    
    /**
     * 租金(原价)
     */
    private BigDecimal rent;
    
    /**
     * 租金(支付价格)
     */
    private BigDecimal rentPayment;
    
    /**
     * 适用类型
     * <pre>
     *     0-全部
     *     1-新租套餐
     *     2-续租套餐
     * </pre>
     *
     * @see ApplicableTypeEnum
     */
    private Integer applicableType;
    
    /**
     * 租金可退
     * <pre>
     *     0-是
     *     1-否
     * </pre>
     *
     * @see YesNoEnum
     */
    private Integer rentRebate;
    
    /**
     * 租金退还期限(天)
     */
    private Integer rentRebateTerm;
    
    /**
     * 租金退还截止时间
     */
    private Long rentRebateEndTime;
    
    /**
     * 实缴押金
     */
    private BigDecimal deposit;
    
    /**
     * 押金缴纳订单编号
     */
    private String depositPayOrderNo;
    
    /**
     * 滞纳金(天)
     */
    private BigDecimal lateFee;
    
    /**
     * 交易方式
     * <pre>
     *     1-线上
     *     2-线下
     * </pre>
     *
     * @see PayTypeEnum
     */
    private Integer payType;
    
    /**
     * 赠送的优惠券ID
     */
    private String couponId;
    
    /**
     * 支付状态
     * <pre>
     *     1-未支付
     *     2-支付成功
     *     3-支付失败
     *     4-取消支付
     * </pre>
     *
     * @see PayStateEnum
     */
    private Integer payState;
    
    /**
     * 使用状态
     * <pre>
     *     1-未使用
     *     2-使用中
     *     3-已失效
     *     4-已退租
     * </pre>
     *
     * @see UseStateEnum
     */
    private Integer useState;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 开始使用时间
     */
    private Long useBeginTime;
    
    /**
     * 套餐押金
     */
    private BigDecimal rentalPackageDeposit;
    
    public List<Long> getCouponIds() {
        if (StrUtil.isNotBlank(this.couponId)) {
            try {
                return JSONUtil.parseArray(couponId, true).toList(String.class)
                        .stream().map(Long::parseLong).collect(Collectors.toList());
            } catch (Throwable e) {
                log.warn("Coupon group conversion error.");
            }
        }
        return ListUtil.empty();
    }
    
    public void setCouponIds(List<Long> couponId) {
        if (CollectionUtil.isEmpty(couponId)) {
            this.couponId = JSONUtil.createArray().toString();
            return;
        }
        try {
            List<String> ids = couponId.stream().map(String::valueOf).distinct().collect(Collectors.toList());
            this.couponId = JSONUtil.toJsonStr(ids);
        } catch (Throwable e) {
            log.warn("Coupon group serializer error.");
        }
    }
}
