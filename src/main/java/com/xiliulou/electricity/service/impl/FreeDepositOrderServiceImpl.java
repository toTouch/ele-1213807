package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.FreeDepositOrderMapper;
import com.xiliulou.electricity.query.FreeBatteryDepositQuery;
import com.xiliulou.electricity.query.ModelBatteryDeposit;
import com.xiliulou.electricity.query.freeBatteryDepositHybridOrderQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.FreeDepositUserInfoVo;
import com.xiliulou.pay.deposit.paixiaozu.exception.PxzFreeDepositException;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzCommonRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositOrderQueryRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositOrderRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzCommonRsp;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzQueryOrderRsp;
import com.xiliulou.pay.deposit.paixiaozu.service.PxzDepositService;
import com.xiliulou.security.bean.TokenUser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * (FreeDepositOrder)表服务实现类
 *
 * @author makejava
 * @since 2023-02-15 11:39:27
 */
@Service("freeDepositOrderService")
@Slf4j
public class FreeDepositOrderServiceImpl implements FreeDepositOrderService {

    @Resource
    private FreeDepositOrderMapper freeDepositOrderMapper;

    @Autowired
    RedisService redisService;

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    ElectricityConfigService electricityConfigService;

    @Autowired
    FranchiseeService franchiseeService;

    @Autowired
    PxzDepositService pxzDepositService;

    @Autowired
    PxzConfigService pxzConfigService;

    @Autowired
    UserBatteryDepositService userBatteryDepositService;

    @Autowired
    EleDepositOrderService eleDepositOrderService;

    @Autowired
    UserBatteryService userBatteryService;

    @Autowired
    ElectricityPayParamsService electricityPayParamsService;

    @Autowired
    UserOauthBindService userOauthBindService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public FreeDepositOrder queryByIdFromDB(Long id) {
        return this.freeDepositOrderMapper.queryById(id);
    }

    @Override
    public FreeDepositOrder selectByOrderId(String orderId) {
        return this.freeDepositOrderMapper.selectOne(new LambdaQueryWrapper<FreeDepositOrder>().eq(FreeDepositOrder::getOrderId, orderId));
    }

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<FreeDepositOrder> queryAllByLimit(int offset, int limit) {
        return this.freeDepositOrderMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param freeDepositOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FreeDepositOrder insert(FreeDepositOrder freeDepositOrder) {
        this.freeDepositOrderMapper.insertOne(freeDepositOrder);
        return freeDepositOrder;
    }

    /**
     * 修改数据
     *
     * @param freeDepositOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(FreeDepositOrder freeDepositOrder) {
        return this.freeDepositOrderMapper.update(freeDepositOrder);
    }

    /**
     * 生成电池免押订单
     * @param freeBatteryDepositQuery
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> freeBatteryDepositOrder(FreeBatteryDepositQuery freeBatteryDepositQuery) {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("FREE DEPOSIT ERROR! not found user info! uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(pxzConfig) || StrUtil.isEmpty(pxzConfig.getAesKey()) || StrUtil.isEmpty(pxzConfig.getMerchantCode())) {
            return Triple.of(false, "100400", "免押功能未配置相关信息！请联系客服处理");
        }

        Triple<Boolean, String, Object> checkUserCanFreeDepositResult = checkUserCanFreeBatteryDeposit(uid, userInfo);
        if (Boolean.FALSE.equals(checkUserCanFreeDepositResult.getLeft())) {
            return checkUserCanFreeDepositResult;
        }

        Triple<Boolean, String, Object> generateDepositOrderResult = generateBatteryDepositOrder(userInfo, freeBatteryDepositQuery);
        if (Boolean.FALSE.equals(generateDepositOrderResult.getLeft())) {
            return generateDepositOrderResult;
        }
        EleDepositOrder eleDepositOrder = (EleDepositOrder) generateDepositOrderResult.getRight();

        FreeDepositOrder freeDepositOrder = FreeDepositOrder.builder().authStatus(FreeDepositOrder.AUTH_PENDING_FREEZE)
                .idCard(freeBatteryDepositQuery.getIdCard())
                .orderId(eleDepositOrder.getOrderId())
                .phone(freeBatteryDepositQuery.getPhoneNumber())
                .realName(freeBatteryDepositQuery.getRealName())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(TenantContextHolder.getTenantId())
                .transAmt(eleDepositOrder.getPayAmount().doubleValue())
                .type(FreeDepositOrder.TYPE_ZHIFUBAO)
                .depositType(FreeDepositOrder.DEPOSIT_TYPE_BATTERY).build();

        PxzCommonRequest<PxzFreeDepositOrderRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(freeDepositOrder.getOrderId());
        query.setMerchantCode(pxzConfig.getMerchantCode());

        PxzFreeDepositOrderRequest request = new PxzFreeDepositOrderRequest();
        request.setPhone(freeBatteryDepositQuery.getPhoneNumber());
        request.setSubject("电池免押");
        request.setRealName(freeBatteryDepositQuery.getRealName());
        request.setIdNumber(freeBatteryDepositQuery.getIdCard());
        request.setTransAmt(BigDecimal.valueOf(freeDepositOrder.getTransAmt()).multiply(BigDecimal.valueOf(100)).intValue());
        query.setData(request);


        PxzCommonRsp<String> callPxzRsp = null;
        try {
            callPxzRsp = pxzDepositService.freeDepositOrder(query);
        } catch (Exception e) {
            log.error("Pxz ERROR! freeDepositOrder fail! uid={},orderId={}", uid, freeDepositOrder.getOrderId(), e);
            return Triple.of(false, "100401", "免押调用失败！");
        }

        if (Objects.isNull(callPxzRsp)) {
            log.error("Pxz ERROR! freeDepositOrder fail! rsp is null! uid={},orderId={}", uid, freeDepositOrder.getOrderId());
            return Triple.of(false, "100401", "免押调用失败！");
        }

        if (!callPxzRsp.isSuccess()) {
            return Triple.of(false, "100401", callPxzRsp.getRespDesc());
        }

        insert(freeDepositOrder);
        eleDepositOrderService.insert(eleDepositOrder);

        //绑定免押订单
        UserBatteryDeposit userBatteryDeposit = new UserBatteryDeposit();
        userBatteryDeposit.setOrderId(eleDepositOrder.getOrderId());
        userBatteryDeposit.setUid(uid);
        userBatteryDeposit.setUpdateTime(System.currentTimeMillis());
        userBatteryDeposit.setDid(eleDepositOrder.getId());
        userBatteryDeposit.setDelFlag(UserBatteryDeposit.DEL_NORMAL);
        userBatteryDeposit.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_FREE);
        userBatteryDeposit.setApplyDepositTime(System.currentTimeMillis());
        userBatteryDeposit.setCreateTime(System.currentTimeMillis());
        userBatteryDeposit.setUpdateTime(System.currentTimeMillis());
        userBatteryDepositService.insertOrUpdate(userBatteryDeposit);

        return Triple.of(true, null, callPxzRsp.getData());
    }

    /**
     * 查询电池免押订单状态
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> selectFreeBatteryDepositOrderStatus() {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        if (!redisService.setNx(CacheConstant.ELE_CACHE_FREE_DEPOSIT_LOCK_KEY + uid, "1", 5 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("FREE DEPOSIT ERROR! not found user info,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(pxzConfig) || StrUtil.isEmpty(pxzConfig.getAesKey()) || StrUtil.isEmpty(pxzConfig.getMerchantCode())) {
            return Triple.of(false, "100400", "免押功能未配置相关信息,请联系客服处理");
        }

        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.error("FREE DEPOSIT ERROR! not found userBatteryDeposit,uid={}", uid);
            return Triple.of(false, "100247", "用户信息不存在");
        }

        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(eleDepositOrder)) {
            log.error("ELE CAR REFUND ERROR! not found eleDepositOrder! uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "ELECTRICITY.0015", "未找到订单");
        }

        FreeDepositOrder freeDepositOrder = this.selectByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            log.error("FREE DEPOSIT ERROR! not found freeDepositOrder,uid={}", uid);
            return Triple.of(false, "100403", "免押订单不存在");
        }

        PxzCommonRequest<PxzFreeDepositOrderQueryRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(userBatteryDeposit.getOrderId());
        query.setMerchantCode(pxzConfig.getMerchantCode());

        PxzFreeDepositOrderQueryRequest request = new PxzFreeDepositOrderQueryRequest();
        query.setData(request);


        PxzCommonRsp<PxzQueryOrderRsp> pxzQueryOrderRsp = null;
        try {
            pxzQueryOrderRsp = pxzDepositService.queryFreeDepositOrder(query);
        } catch (PxzFreeDepositException e) {
            log.error("Pxz ERROR! freeDepositOrderQuery fail! uid={},orderId={}", uid, userBatteryDeposit.getOrderId(), e);
            return Triple.of(false, "100402", "免押查询失败！");
        }

        if (Objects.isNull(pxzQueryOrderRsp)) {
            log.error("Pxz ERROR! freeDepositOrderQuery fail! pxzQueryOrderRsp is null! uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "100402", "免押查询失败！");
        }

        if (!pxzQueryOrderRsp.isSuccess()) {
            return Triple.of(false, "100402", pxzQueryOrderRsp.getRespDesc());
        }

        PxzQueryOrderRsp queryOrderRspData = pxzQueryOrderRsp.getData();
        if (Objects.isNull(queryOrderRspData)) {
            log.error("Pxz ERROR! freeDepositOrderQuery fail! queryOrderRspData is null! uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "100402", "免押查询失败！");
        }

        //更新免押订单状态
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setAuthNo(queryOrderRspData.getAuthNo());
        freeDepositOrderUpdate.setAuthStatus(queryOrderRspData.getAuthStatus());
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(freeDepositOrderUpdate);

        //冻结成功
        if (Objects.equals(queryOrderRspData.getAuthStatus(), FreeDepositOrder.AUTH_FROZEN)) {
            //更新押金订单状态
            EleDepositOrder eleDepositOrderUpdate = new EleDepositOrder();
            eleDepositOrderUpdate.setId(eleDepositOrder.getId());
            eleDepositOrderUpdate.setStatus(EleDepositOrder.STATUS_SUCCESS);
            eleDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
            eleDepositOrderService.update(eleDepositOrderUpdate);

            //绑定加盟商、更新押金状态
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setUid(uid);
            userInfoUpdate.setFranchiseeId(eleDepositOrder.getFranchiseeId());
            userInfoUpdate.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_YES);
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(userInfoUpdate);

            //绑定电池型号
            UserBattery userBattery = new UserBattery();
            userBattery.setUid(uid);
            userBattery.setBatteryType(eleDepositOrder.getBatteryType());
            userBattery.setUpdateTime(System.currentTimeMillis());
            userBatteryService.insertOrUpdate(userBattery);
        }

        FreeDepositUserInfoVo freeDepositUserInfoVo = new FreeDepositUserInfoVo();
        freeDepositUserInfoVo.setName(userInfo.getName());
        freeDepositUserInfoVo.setPhone(userInfo.getPhone());
        freeDepositUserInfoVo.setIdCard(userInfo.getIdNumber());

        freeDepositUserInfoVo.setFranchiseeId(eleDepositOrder.getFranchiseeId());
        freeDepositUserInfoVo.setModel(StringUtils.isNoneBlank(eleDepositOrder.getBatteryType()) ? BatteryConstant.acquireBatteryModel(eleDepositOrder.getBatteryType()) : null);
        freeDepositUserInfoVo.setBatteryDepositType(userBatteryDeposit.getDepositType());
        freeDepositUserInfoVo.setApplyBatteryDepositTime(userBatteryDeposit.getApplyDepositTime());
        freeDepositUserInfoVo.setBatteryDepositAuthStatus(queryOrderRspData.getAuthStatus());

        return Triple.of(true, null, freeDepositUserInfoVo);
    }

    /**
     * 电池免押 套餐、保险混合支付
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> freeDepositHybridOrder(freeBatteryDepositHybridOrderQuery query, HttpServletRequest request) {

        Integer tenantId = TenantContextHolder.getTenantId();

        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        if (!redisService.setNx(CacheConstant.ELE_CACHE_FREE_DEPOSIT_MEMBERCARD_LOCK_KEY + uid, "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("FREE DEPOSIT HYBRID ERROR! not found user info,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELE CAR DEPOSIT ERROR! not found userInfo,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("ELE CAR DEPOSIT ERROR! user not auth,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }

        //支付相关
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
        if (Objects.isNull(electricityPayParams)) {
            log.error("ELE CAR DEPOSIT ERROR!not found electricityPayParams,uid={}", uid);
            return Triple.of(false, "100234", "未配置支付参数!");
        }

        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(uid, tenantId);
        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.error("ELE CAR DEPOSIT ERROR!not found userOauthBind,uid={}", uid);
            return Triple.of(false, "100235", "未找到用户的第三方授权信息!");
        }









        return null;
    }

    @Override
    public Triple<Boolean, String, Object> freeBatteryDepositPreCheck() {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("FREE DEPOSIT ERROR! not found user info! uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        Triple<Boolean, String, Object> checkResult = checkUserCanFreeBatteryDeposit(uid, userInfo);
        if (Boolean.FALSE.equals(checkResult.getLeft())) {
            return checkResult;
        }

        FreeDepositUserInfoVo freeDepositUserInfoVo = new FreeDepositUserInfoVo();
        freeDepositUserInfoVo.setName(userInfo.getName());
        freeDepositUserInfoVo.setPhone(userInfo.getPhone());
        freeDepositUserInfoVo.setIdCard(userInfo.getIdNumber());

        return Triple.of(true, null, freeDepositUserInfoVo);
    }

    @Override
    public Triple<Boolean, String, Object> freeCarDepositPreCheck() {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("FREE DEPOSIT ERROR! not found user info! uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        Triple<Boolean, String, Object> checkResult = checkUserCanFreeCarDeposit(uid, userInfo);
        if (Boolean.FALSE.equals(checkResult.getLeft())) {
            return checkResult;
        }

        FreeDepositUserInfoVo freeDepositUserInfoVo = new FreeDepositUserInfoVo();
        freeDepositUserInfoVo.setName(userInfo.getName());
        freeDepositUserInfoVo.setPhone(userInfo.getPhone());
        freeDepositUserInfoVo.setIdCard(userInfo.getIdNumber());

        return Triple.of(true, null, freeDepositUserInfoVo);
    }

    private Triple<Boolean, String, Object> checkUserCanFreeBatteryDeposit(Long uid, UserInfo userInfo) {
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        if (!Objects.equals(electricityConfig.getFreeDepositType(), ElectricityConfig.FREE_DEPOSIT_TYPE_BATTERY) && !Objects.equals(electricityConfig.getFreeDepositType(), ElectricityConfig.FREE_DEPOSIT_TYPE_ALL)) {
            return Triple.of(false, null, "押金免押功能未开启,请联系客服处理");
        }

        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("FREE DEPOSIT ERROR! user is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("FREE DEPOSIT ERROR! user not auth,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }

        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            return Triple.of(false, "ELECTRICITY.0049", "电池押金已经缴纳，无需重复缴纳");
        }
        return Triple.of(true, null, null);
    }

    private Triple<Boolean, String, Object> checkUserCanFreeCarDeposit(Long uid, UserInfo userInfo) {
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        if (!Objects.equals(electricityConfig.getFreeDepositType(), ElectricityConfig.FREE_DEPOSIT_TYPE_BATTERY) && !Objects.equals(electricityConfig.getFreeDepositType(), ElectricityConfig.FREE_DEPOSIT_TYPE_ALL)) {
            return Triple.of(false, null, "押金免押功能未开启,请联系客服处理");
        }

        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("FREE DEPOSIT ERROR! user is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("FREE DEPOSIT ERROR! user not auth,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }

        if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            return Triple.of(false, "ELECTRICITY.0049", "车辆押金已经缴纳，无需重复缴纳");
        }
        return Triple.of(true, null, null);
    }

    private Triple<Boolean, String, Object> generateBatteryDepositOrder(UserInfo userInfo, FreeBatteryDepositQuery freeBatteryDepositQuery) {

        BigDecimal depositPayAmount = null;

        Franchisee franchisee = franchiseeService.queryByIdFromCache(freeBatteryDepositQuery.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("payDeposit  ERROR! not found Franchisee ！franchiseeId={},uid={}", freeBatteryDepositQuery.getFranchiseeId(), userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0038", "未找到加盟商");
        }

        if (Objects.equals(franchisee.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
            depositPayAmount = franchisee.getBatteryDeposit();
        }

        //型号押金计算
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            if (Objects.isNull(freeBatteryDepositQuery.getModel())) {
                return Triple.of(false, "ELECTRICITY.0007", "不合法的参数");
            }

            //型号押金
            List<ModelBatteryDeposit> modelBatteryDepositList = JsonUtil.fromJsonArray(
                    franchisee.getModelBatteryDeposit(), ModelBatteryDeposit.class);
            if (ObjectUtil.isEmpty(modelBatteryDepositList)) {
                log.error("payDeposit  ERROR! not found modelBatteryDepositList ！franchiseeId={},uid={}",
                        freeBatteryDepositQuery.getFranchiseeId(), userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.00110", "未找到押金");
            }

            for (ModelBatteryDeposit modelBatteryDeposit : modelBatteryDepositList) {
                if ((double) (modelBatteryDeposit.getModel()) - freeBatteryDepositQuery.getModel() < 1
                        && (double) (modelBatteryDeposit.getModel()) - freeBatteryDepositQuery.getModel() >= 0) {
                    depositPayAmount = modelBatteryDeposit.getBatteryDeposit();
                    break;
                }
            }
        }

        if (Objects.isNull(depositPayAmount)) {
            log.error("payDeposit  ERROR! payAmount is null ！franchiseeId{},uid={}", freeBatteryDepositQuery.getFranchiseeId(),
                    userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.00110", "未找到押金");
        }

        //电池型号
        String batteryType = Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE) ? BatteryConstant.acquireBatteryShort(freeBatteryDepositQuery.getModel()) : null;

        //生成押金独立订单
        String depositOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT, userInfo.getUid());
        EleDepositOrder eleDepositOrder = EleDepositOrder.builder().orderId(depositOrderId).uid(userInfo.getUid())
                .phone(userInfo.getPhone()).name(userInfo.getName()).payAmount(depositPayAmount)
                .status(EleDepositOrder.STATUS_INIT).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).tenantId(userInfo.getTenantId())
                .franchiseeId(franchisee.getId()).payType(EleDepositOrder.FREE_DEPOSIT_PAYMENT).storeId(null)
                .modelType(franchisee.getModelType())
                .batteryType(batteryType).build();

        return Triple.of(true, null, eleDepositOrder);
    }
}
