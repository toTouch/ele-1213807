package com.xiliulou.electricity.entity.car;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.basic.BasicCarPo;
import com.xiliulou.electricity.enums.ApplicableTypeEnum;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.enums.PayTypeEnum;
import com.xiliulou.electricity.enums.RenalPackageConfineEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.enums.RentalUnitEnum;
import com.xiliulou.electricity.enums.UseStateEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
    private Long couponId;
    
    /**
     * <p>
     * Description: 优惠劵组id,JSON数组格式
     * </p>
     */
    private String couponArrays;
    
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
    
    /**
     * 微信商户号
     */
    private String wechatMerchantId;
    
    /**
     * 支付加盟商ID
     */
    private Long payFranchiseeId;
    
    public List<Long> getCouponIds() {
        Set<Long> result = new HashSet<>();
        if (StrUtil.isNotBlank(this.couponArrays)) {
            List<Long> longs = JsonUtil.fromJsonArray(this.couponArrays, Long.class);
            if (!CollectionUtils.isEmpty(longs)) {
                result.addAll(longs);
            }
        }
        if (!Objects.isNull(this.couponId)) {
            result.add(this.couponId);
        }
        return new ArrayList<>(result);
    }
    
    public void setCouponIds(List<Long> couponId) {
        if (CollectionUtil.isEmpty(couponId)) {
            this.couponArrays = JSONUtil.createArray().toString();
            return;
        }
        this.couponArrays = JsonUtil.toJson(new HashSet<>(couponId));
    }
}
