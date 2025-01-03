package com.xiliulou.electricity.entity.car;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.basic.BasicCarPo;
import com.xiliulou.electricity.enums.ApplicableTypeEnum;
import com.xiliulou.electricity.enums.RenalPackageConfineEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.enums.RentalUnitEnum;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 租车套餐持久类
 *
 * @author xiaohui.song
 **/

@Data
@Slf4j
@TableName("t_car_rental_package")
public class CarRentalPackagePo extends BasicCarPo {
    
    private static final long serialVersionUID = -5562928515712317577L;
    
    /**
     * 套餐名称
     */
    private String name;
    
    /**
     * 套餐类型
     * <pre>
     *     1-单车
     *     2-车电一体
     * </pre>
     *
     * @see RentalPackageTypeEnum
     */
    private Integer type;
    
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
     * 租金
     */
    private BigDecimal rent;
    
    /**
     * 押金
     */
    private BigDecimal deposit;
    
    /**
     * 车辆型号ID
     */
    private Integer carModelId;
    
    /**
     * 电池型号对应的电压伏数
     */
    private String batteryVoltage;
    
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
     * 免押
     * <pre>
     *     0-是
     *     1-否
     * </pre>
     *
     * @see YesNoEnum
     */
    private Integer freeDeposit;
    
    /**
     * 租金单价，单位同租期单位
     */
    private BigDecimal rentUnitPrice;
    
    /**
     * 滞纳金(天)
     */
    private BigDecimal lateFee;
    
    
    /**
     * 冻结滞纳金
     */
    private BigDecimal freezeLateFee;
    
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
     * 优惠券赠送
     * <pre>
     *     0-是
     *     1-否
     * </pre>
     *
     * @see YesNoEnum
     */
    private Integer giveCoupon;
    
    /**
     * 优惠券ID
     */
    private Long couponId;
    
    /**
     * <p>
     * Description: 优惠劵组id,JSON数组格式
     * </p>
     */
    private String couponArrays;
    
    /**
     * 上下架状态
     * <pre>
     *     0-上架
     *     1-下架
     * </pre>
     *
     * @see UpDownEnum
     */
    private Integer status;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 排序参数，用户端排序使用
     */
    private Long sortParam;
    
    /**
     * <p>
     * Description: 用户自定义分组id
     * </p>
     */
    private String userGroupIds;
    
    /**
     * <p>
     * Description: 用户是否为自定义分组
     * <pre>
     *        0 -- 系统分组
     *        1 -- 自定义分组
     *    </pre>
     * </p>
     */
    private Integer isUserGroup;
    
    
    public List<Long> getCouponIds() {
        Set<Long> result = new HashSet<>();
        if (StrUtil.isNotBlank(this.couponArrays)) {
            List<Long> longs = JsonUtil.fromJsonArray(this.couponArrays, Long.class);
            if (!CollectionUtils.isEmpty(longs)) {
                result.addAll(longs);
            }
        }
        if (!Objects.isNull(this.couponId) && !Objects.equals(this.couponId, -1L)) {
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
    
    
    public List<Long> getUserGroupId() {
        if (StrUtil.isNotBlank(this.userGroupIds)) {
            return JsonUtil.fromJsonArray(this.userGroupIds, Long.class);
        }
        return Collections.emptyList();
    }
    
    public void setUserGroupId(List<Long> userGroupId) {
        if (CollectionUtil.isEmpty(userGroupId)) {
            this.userGroupIds = JSONUtil.createArray().toString();
            return;
        }
        this.userGroupIds = JsonUtil.toJson(new HashSet<>(userGroupId));
    }
}
