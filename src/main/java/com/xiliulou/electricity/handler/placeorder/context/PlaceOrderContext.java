package com.xiliulou.electricity.handler.placeorder.context;

import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.query.PlaceOrderQuery;
import com.xiliulou.security.bean.TokenUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * @Description 责任链上下文
 * @Author: SongJP
 * @Date: 2024/10/24 10:42
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderContext {
    
    /**
     * 购买下单请求参数
     */
    private PlaceOrderQuery placeOrderQuery;
    
    /**
     * 换电套餐
     */
    private BatteryMemberCard batteryMemberCard;
    
    /**
     * 支付参数
     */
    private BasePayConfig payParamConfig;
    
    /**
     * 用户第三方授权信息
     */
    private UserOauthBind userOauthBind;
    
    /**
     * 购买下单的用户
     */
    private UserInfo userInfo;
    
    /**
     * 登录用户的用户信息
     */
    private TokenUser tokenUser;
    
    /**
     * 登录校验设置的租户id
     */
    private Integer tenantId;
    
    /**
     * 扫码的柜机
     */
    private ElectricityCabinet electricityCabinet;
    
    /**
     * 用户要购买的保险
     */
    private FranchiseeInsurance franchiseeInsurance;
    
    /**
     * 支付押金生成的押金订单
     */
    private EleDepositOrder eleDepositOrder;
    
    /**
     * 购买套餐生成的套餐订单
     */
    private ElectricityMemberCardOrder electricityMemberCardOrder;
    
    /**
     * 购买保险生成的保险订单
     */
    private InsuranceOrder insuranceOrder;
    
    /**
     * 购买下单各子订单订单号
     */
    private List<String> orderList;
    
    /**
     * 购买下单各子订单订单类型
     */
    private List<Integer> orderTypeList;
    
    /**
     * 购买下单各子订单订单金额
     */
    private List<BigDecimal> allPayAmount;
    
    /**
     * 购买下单总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 用户使用的优惠券
     */
    private Set<Integer> userCouponIds;
    
    /**
     * 请求对象
     */
    private HttpServletRequest request;
    
    /**
     * 租户配置
     */
    private ElectricityConfig electricityConfig;
}
