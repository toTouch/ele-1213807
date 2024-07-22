package com.xiliulou.electricity.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.dto.FranchiseeInsuranceCarModelAndBatteryTypeDTO;
import com.xiliulou.electricity.entity.City;
import com.xiliulou.electricity.entity.CommonPayOrder;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.InsuranceOrderMapper;
import com.xiliulou.electricity.query.InsuranceOrderAdd;
import com.xiliulou.electricity.query.InsuranceOrderQuery;
import com.xiliulou.electricity.service.BatteryModelService;
import com.xiliulou.electricity.service.CityService;
import com.xiliulou.electricity.service.ElectricityCarModelService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.FranchiseeInsuranceService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.service.UserBatteryService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.InsuranceOrderVO;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.dto.FranchiseeInsuranceCarModelAndBatteryTypeDTO.BATTERY_TYPE;

/**
 * 换电柜保险用户绑定(FranchiseeInsurance)表服务接口
 *
 * @author makejava
 * @since 2022-11-02 13:37:11
 */
@Service("insuranceOrderService")
@Slf4j
public class InsuranceOrderServiceImpl extends ServiceImpl<InsuranceOrderMapper, InsuranceOrder> implements InsuranceOrderService {
    
    @Resource
    InsuranceOrderMapper insuranceOrderMapper;
    
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    
    @Autowired
    UserOauthBindService userOauthBindService;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    FranchiseeInsuranceService franchiseeInsuranceService;
    
    @Autowired
    FranchiseeService franchiseeService;
    
    @Autowired
    InsuranceUserInfoService insuranceUserInfoService;
    
    @Autowired
    ElectricityTradeOrderService electricityTradeOrderService;
    
    @Autowired
    ElectricityConfigService electricityConfigService;
    
    @Autowired
    CityService cityService;
    
    @Autowired
    UserBatteryService userBatteryService;
    
    @Autowired
    BatteryModelService batteryModelService;
    
    @Autowired
    UserBatteryTypeService userBatteryTypeService;
    
    @Autowired
    ElectricityCarModelService electricityCarModelService;
    
    @Autowired
    private UserDataScopeService userDataScopeService;
    
    @Autowired
    private WechatPayParamsBizService wechatPayParamsBizService;
    
    /**
     * 根据来源订单编码、类型查询保险订单信息
     *
     * @param sourceOrderNo 来源订单编码
     * @param insuranceType 类型：0-电、1-车、2-车电
     * @return 保险订单
     */
    @Slave
    @Override
    public InsuranceOrder selectBySourceOrderNoAndType(String sourceOrderNo, Integer insuranceType) {
        if (!ObjectUtils.allNotNull(sourceOrderNo, insuranceType)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        
        LambdaQueryWrapper<InsuranceOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InsuranceOrder::getSourceOrderNo, sourceOrderNo).eq(InsuranceOrder::getInsuranceType, insuranceType);
        return insuranceOrderMapper.selectOne(queryWrapper);
    }
    
    @Slave
    @Override
    public R queryList(InsuranceOrderQuery insuranceOrderQuery,Boolean isType) {
        if (!isType){
            return R.ok(queryListByStatus(insuranceOrderQuery));
        }
        List<InsuranceOrderVO> insuranceOrderVOS = queryListByStatus(insuranceOrderQuery);
        if (CollectionUtil.isEmpty(insuranceOrderVOS)){
            return R.ok(ListUtil.empty());
        }
        Set<Long> collect = insuranceOrderVOS.stream().map(InsuranceOrderVO::getInsuranceId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (CollectionUtil.isEmpty(collect)){
            return R.ok(insuranceOrderVOS);
        }
        /*1.t_electricity_car_model -- name 车辆型号表*/
        /*2.t_franchisee_insurance -- car_model_id,simple_battery_type 保险配置表*/
        List<FranchiseeInsuranceCarModelAndBatteryTypeDTO> result = franchiseeInsuranceService.selectListCarModelAndBatteryTypeById(collect);
        if (CollectionUtil.isEmpty(result)){
            return R.ok(insuranceOrderVOS);
        }
        Map<Long, FranchiseeInsuranceCarModelAndBatteryTypeDTO> dtoMap = result.stream().collect(Collectors.toMap(FranchiseeInsuranceCarModelAndBatteryTypeDTO::getId, v -> v));
        for (InsuranceOrderVO i : insuranceOrderVOS) {
            if (!dtoMap.containsKey(i.getInsuranceId())){
                continue;
            }
            FranchiseeInsuranceCarModelAndBatteryTypeDTO dto = dtoMap.get(i.getInsuranceId());
            if (Objects.equals(BATTERY_TYPE,dto.getInsuranceType())){
                i.setBatteryModel(dto.getLabel());
                continue;
            }
            i.setCarModel(dto.getLabel());
        }
        return R.ok(insuranceOrderVOS);
    }
    
    @Slave
    @Override
    public R queryCount(InsuranceOrderQuery insuranceOrderQuery) {
        return R.ok(insuranceOrderMapper.queryCount(insuranceOrderQuery));
    }
    
    @Override
    public R createOrder(InsuranceOrderAdd insuranceOrderAdd, HttpServletRequest request) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("rentBattery  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(user.getUid(), tenantId);
        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.error("CREATE INSURANCE_ORDER ERROR ,NOT FOUND USEROAUTHBIND OR THIRDID IS NULL  UID={}", user.getUid());
            return R.failMsg("未找到用户的第三方授权信息!");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("CREATE INSURANCE_ORDER ERROR! not found user,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("CREATE INSURANCE_ORDER ERROR! user is unUsable! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("CREATE INSURANCE_ORDER ERROR! user not auth! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }
        
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.error("CREATE INSURANCE_ORDER ERROR! not pay deposit,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromDB(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("CREATE INSURANCE_ORDER ERROR! not found Franchisee ！franchiseeId={}", userInfo.getFranchiseeId());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }
        
        InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(user.getUid());
        if (Objects.nonNull(insuranceUserInfo) && Objects.equals(insuranceUserInfo.getIsUse(), InsuranceUserInfo.NOT_USE)
                && insuranceUserInfo.getInsuranceExpireTime() > System.currentTimeMillis()) {
            log.error("CREATE INSURANCE_ORDER ERROR! user have insurance ！uid={}", userInfo.getUid());
            return R.fail("100310", "已购买保险");
        }
        
        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByIdFromCache(insuranceOrderAdd.getInsuranceId());
        if (Objects.isNull(franchiseeInsurance)) {
            log.error("CREATE INSURANCE_ORDER ERROR,NOT FOUND MEMBER_CARD BY ID={}", insuranceOrderAdd.getInsuranceId());
            return R.fail("100305", "未找到保险!");
        }
        
        WechatPayParamsDetails wechatPayParamsDetails = null;
        try {
            wechatPayParamsDetails = wechatPayParamsBizService.getDetailsByIdTenantIdAndFranchiseeId(tenantId, franchiseeInsurance.getFranchiseeId());
        } catch (WechatPayException e) {
            log.error("CREATE INSURANCE_ORDER ERROR ,NOT FOUND PAY_PARAMS");
            return R.fail("PAY_TRANSFER.0019", "支付未成功，请联系客服处理");
        }
        if (Objects.isNull(wechatPayParamsDetails)) {
            log.error("CREATE INSURANCE_ORDER ERROR ,NOT FOUND PAY_PARAMS");
            return R.fail("100307", "未配置支付参数!");
        }
        
        if (ObjectUtil.equal(FranchiseeInsurance.STATUS_UN_USABLE, franchiseeInsurance.getStatus())) {
            log.error("CREATE INSURANCE_ORDER ERROR ,MEMBER_CARD IS UN_USABLE ID={}", insuranceOrderAdd.getInsuranceId());
            return R.fail("100306", "保险已禁用!");
        }
        
        if (Objects.isNull(franchiseeInsurance.getPremium())) {
            log.error("CREATE INSURANCE_ORDER ERROR! payAmount is null ！franchiseeId={}", userInfo.getFranchiseeId());
            return R.fail("100305", "未找到保险");
        }
        
        if (Objects.nonNull(insuranceUserInfo)) {
            FranchiseeInsurance userBindFranchiseeInsurance = franchiseeInsuranceService.queryByIdFromCache(insuranceUserInfo.getInsuranceId());
            if (Objects.nonNull(userBindFranchiseeInsurance) && !Objects.equals(userBindFranchiseeInsurance.getSimpleBatteryType(), franchiseeInsurance.getSimpleBatteryType())) {
                return R.fail("100310", "保险类型不一致");
            }
        }
        
        //生成保险订单
        String orderId = generateOrderId(user.getUid());
        
        InsuranceOrder insuranceOrder = InsuranceOrder.builder().insuranceId(franchiseeInsurance.getId()).insuranceName(franchiseeInsurance.getName())
                .insuranceType(FranchiseeInsurance.INSURANCE_TYPE_BATTERY).orderId(orderId).cid(franchiseeInsurance.getCid()).franchiseeId(franchisee.getId())
                .isUse(InsuranceOrder.NOT_USE).payAmount(franchiseeInsurance.getPremium()).forehead(franchiseeInsurance.getForehead()).payType(InsuranceOrder.ONLINE_PAY_TYPE)
                .phone(userInfo.getPhone()).status(InsuranceOrder.STATUS_INIT).tenantId(tenantId).uid(user.getUid()).userName(userInfo.getName())
                .validDays(franchiseeInsurance.getValidDays()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
        
        //支付零元
        if (franchiseeInsurance.getPremium().compareTo(BigDecimal.valueOf(0.01)) < 0) {
            insuranceOrder.setStatus(InsuranceOrder.STATUS_SUCCESS);
            insuranceOrderMapper.insert(insuranceOrder);
            
            InsuranceUserInfo updateOrAddInsuranceUserInfo = new InsuranceUserInfo();
            updateOrAddInsuranceUserInfo.setUid(userInfo.getUid());
            updateOrAddInsuranceUserInfo.setUpdateTime(System.currentTimeMillis());
            updateOrAddInsuranceUserInfo.setIsUse(InsuranceUserInfo.NOT_USE);
            updateOrAddInsuranceUserInfo.setInsuranceOrderId(orderId);
            updateOrAddInsuranceUserInfo.setInsuranceId(franchiseeInsurance.getId());
            updateOrAddInsuranceUserInfo.setInsuranceExpireTime(System.currentTimeMillis() + franchiseeInsurance.getValidDays() * ((24 * 60 * 60 * 1000L)));
            updateOrAddInsuranceUserInfo.setTenantId(tenantId);
            updateOrAddInsuranceUserInfo.setForehead(franchiseeInsurance.getForehead());
            updateOrAddInsuranceUserInfo.setPremium(franchiseeInsurance.getPremium());
            updateOrAddInsuranceUserInfo.setFranchiseeId(franchisee.getId());
            
            if (Objects.isNull(insuranceUserInfo)) {
                updateOrAddInsuranceUserInfo.setCreateTime(System.currentTimeMillis());
                insuranceUserInfoService.insert(updateOrAddInsuranceUserInfo);
            } else {
                insuranceUserInfoService.update(updateOrAddInsuranceUserInfo);
            }
            return R.ok();
        }
        insuranceOrderMapper.insert(insuranceOrder);
        
        //调起支付
        try {
            CommonPayOrder commonPayOrder = CommonPayOrder.builder().orderId(orderId).uid(user.getUid()).payAmount(franchiseeInsurance.getPremium())
                    .orderType(ElectricityTradeOrder.ORDER_TYPE_INSURANCE).attach(ElectricityTradeOrder.ATTACH_INSURANCE).description("保险收费").tenantId(tenantId).build();
            
            WechatJsapiOrderResultDTO resultDTO = electricityTradeOrderService.commonCreateTradeOrderAndGetPayParams(commonPayOrder, wechatPayParamsDetails,
                    userOauthBind.getThirdId(), request);
            return R.ok(resultDTO);
        } catch (WechatPayException e) {
            log.error("CREATE INSURANCE_ORDER ERROR! wechat v3 order  error! uid={}", user.getUid(), e);
        }
        
        return R.fail("PAY_TRANSFER.0019", "支付未成功，请联系客服处理");
    }
    
    @Slave
    @Override
    public InsuranceOrder queryByOrderId(String orderNo) {
        return insuranceOrderMapper.selectOne(new LambdaQueryWrapper<InsuranceOrder>().eq(InsuranceOrder::getOrderId, orderNo));
    }
    
    @Override
    public Integer updateOrderStatusById(InsuranceOrder insuranceOrder) {
        return insuranceOrderMapper.updateById(insuranceOrder);
    }
    
    @Override
    public int updateIsUseByOrderId(InsuranceOrder insuranceOrder) {
        return insuranceOrderMapper.updateIsUseByOrderId(insuranceOrder);
    }
    
    @Slave
    @Override
    public R queryInsurance() {
        
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("queryInsurance  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("queryInsurance  ERROR! not found user,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
        if (Objects.isNull(electricityConfig) || Objects.equals(electricityConfig.getIsOpenInsurance(), ElectricityConfig.DISABLE_INSURANCE)) {
            return R.ok();
        }
        
        // TODO: 2023/1/6  HPBUG
/*        UserBattery userBattery = userBatteryService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBattery)) {
            log.error("queryInsurance  ERROR! not pay deposit,uid={}", user.getUid());
            //返回成功为了兼容未更新的小程序
            return R.ok();
        }*/
        String batteryType = userBatteryTypeService.selectUserSimpleBatteryType(userInfo.getUid());
        
        return R.ok(franchiseeInsuranceService.queryByFranchiseeIdAndSimpleBatteryType(userInfo.getFranchiseeId(), batteryType, tenantId));
    }
    
    @Slave
    @Override
    public R homeOneQueryInsurance(Integer model, Long franchiseeId) {
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("queryInsurance  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("queryInsurance  ERROR! not found user,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
        if (Objects.isNull(electricityConfig) || Objects.equals(electricityConfig.getIsOpenInsurance(), ElectricityConfig.DISABLE_INSURANCE)) {
            //            log.error("queryInsurance  ERROR! not found insurance！franchiseeId={}", userInfo.getFranchiseeId());
            return R.ok();
        }
        
        String batteryType = null;
        String batteryV = null;
        if (Objects.nonNull(model)) {
            batteryType = batteryModelService.acquireBatteryShort(model, tenantId);
            if (StringUtils.isNotBlank(batteryType)) {
                batteryV = batteryType.substring(batteryType.indexOf("_") + 1).substring(0, batteryType.substring(batteryType.indexOf("_") + 1).indexOf("_"));
            }
        }
        return R.ok(franchiseeInsuranceService.queryByFranchiseeId(franchiseeId, batteryV, tenantId));
        
    }
    
    @Override
    public void insert(InsuranceOrder insuranceOrder) {
        insuranceOrderMapper.insert(insuranceOrder);
    }
    
    @Override
    public Integer update(InsuranceOrder insuranceOrder) {
        return insuranceOrderMapper.updateById(insuranceOrder);
    }
    
    @Override
    public Integer updateUseStatusByOrderId(String insuranceOrderId, Integer useStatus) {
        if (StringUtils.isBlank(insuranceOrderId)) {
            return NumberConstant.ZERO;
        }
        return insuranceOrderMapper.updateUseStatusByOrderId(insuranceOrderId, useStatus);
    }
    
    @Override
    public Integer updateUseStatusForRefund(String insuranceOrderId, Integer useStatus) {
        if (StringUtils.isBlank(insuranceOrderId)) {
            return NumberConstant.ZERO;
        }
        return insuranceOrderMapper.updateUseStatusForRefund(insuranceOrderId, useStatus);
    }
    
    /**
     * 更新用户手机号
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    @Override
    public Integer updatePhoneByUid(Integer tenantId, Long uid, String newPhone) {
        return insuranceOrderMapper.updatePhoneByUid(tenantId, uid, newPhone);
    }
    
    public String generateOrderId(Long uid) {
        return String.valueOf(System.currentTimeMillis()).substring(0, 6) + uid + RandomUtil.randomNumbers(4);
    }
    
    @Override
    public Triple<Boolean, String, Object> handleRentBatteryInsurance(Integer insuranceId, UserInfo userInfo) {
        if (Objects.isNull(insuranceId)) {
            return Triple.of(true, "", null);
        }
        
        //查询保险
        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByIdFromCache(insuranceId);
        
        if (Objects.isNull(franchiseeInsurance)) {
            log.error("CREATE INSURANCE_ORDER ERROR!not found member_card by id={},uid={}", insuranceId, userInfo.getUid());
            return Triple.of(false, "100305", "未找到保险!");
        }
        if (ObjectUtil.equal(FranchiseeInsurance.STATUS_UN_USABLE, franchiseeInsurance.getStatus())) {
            log.error("CREATE INSURANCE_ORDER ERROR!member_card is un_usable id={},uid={}", insuranceId, userInfo.getUid());
            return Triple.of(false, "100306", "保险已禁用!");
        }
        
        if (Objects.isNull(franchiseeInsurance.getPremium())) {
            log.error("CREATE INSURANCE_ORDER ERROR!payAmount is null ！franchiseeId={},uid={}", insuranceId, userInfo.getUid());
            return Triple.of(false, "100305", "未找到保险");
        }
        
        //生成保险独立订单
        String insuranceOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_INSURANCE, userInfo.getUid());
        InsuranceOrder insuranceOrder = InsuranceOrder.builder()
                .insuranceId(franchiseeInsurance.getId())
                .insuranceName(franchiseeInsurance.getName())
                .insuranceType(FranchiseeInsurance.INSURANCE_TYPE_BATTERY)
                .orderId(insuranceOrderId)
                .cid(franchiseeInsurance.getCid())
                .franchiseeId(franchiseeInsurance.getFranchiseeId())
                .isUse(InsuranceOrder.NOT_USE)
                .payAmount(franchiseeInsurance.getPremium())
                .forehead(franchiseeInsurance.getForehead())
                .payType(InsuranceOrder.ONLINE_PAY_TYPE)
                .phone(userInfo.getPhone())
                .status(InsuranceOrder.STATUS_INIT)
                .tenantId(userInfo.getTenantId())
                .storeId(userInfo.getStoreId())
                .uid(userInfo.getUid())
                .userName(userInfo.getName())
                .validDays(franchiseeInsurance.getValidDays())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .simpleBatteryType(franchiseeInsurance.getSimpleBatteryType()).build();

        return Triple.of(true, "", insuranceOrder);
    }
    
    @Slave
    @Override
    public List<InsuranceOrderVO> queryListByStatus(InsuranceOrderQuery insuranceOrderQuery) {
        List<InsuranceOrderVO> insuranceOrderVOList = insuranceOrderMapper.queryList(insuranceOrderQuery);
        if (ObjectUtil.isEmpty(insuranceOrderVOList)) {
            return new ArrayList<>();
        }
        
        insuranceOrderVOList.parallelStream().forEach(e -> {
            
            //获取城市名称
            City city = cityService.queryByIdFromDB(e.getCid());
            if (Objects.nonNull(city)) {
                e.setCityName(city.getName());
            }
            
            Integer validDays = e.getValidDays();
            Long insuranceExpireTime = validDays * (24 * 60 * 60 * 1000L);
            e.setInsuranceExpireTime(insuranceExpireTime);
        });
        return insuranceOrderVOList;
    }
    
}
