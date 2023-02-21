package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.entity.PxzConfig;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.FreeDepositOrderMapper;
import com.xiliulou.electricity.query.FreeDepositQuery;
import com.xiliulou.electricity.query.ModelBatteryDeposit;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xiliulou.electricity.service.PxzConfigService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.FreeDepositUserInfoVo;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzCommonRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositOrderRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzCommonRsp;
import com.xiliulou.pay.deposit.paixiaozu.service.PxzDepositService;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.sql.Struct;
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
    
    
    @Override
    public Triple<Boolean, String, Object> freeDepositOrder(FreeDepositQuery freeDepositQuery) {
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
        if (Objects.isNull(pxzConfig) || StrUtil.isEmpty(pxzConfig.getAesKey()) || StrUtil.isEmpty(
                pxzConfig.getMerchantCode())) {
            return Triple.of(false, "100400", "免押功能未配置相关信息！请联系客服处理");
        }
        
        Triple<Boolean, String, Object> checkUserCanFreeDepositResult = checkUserCanFreeDeposit(uid, userInfo);
        if (Boolean.FALSE.equals(checkUserCanFreeDepositResult.getLeft())) {
            return checkUserCanFreeDepositResult;
        }
        
        Triple<Boolean, String, Object> generateDepositOrderResult = generateDepositOrder(userInfo, freeDepositQuery);
        if (Boolean.FALSE.equals(generateDepositOrderResult.getLeft())) {
            return generateDepositOrderResult;
        }
        EleDepositOrder eleDepositOrder = (EleDepositOrder) generateDepositOrderResult.getRight();
        
        FreeDepositOrder freeDepositOrder = FreeDepositOrder.builder().authStatus(FreeDepositOrder.AUTH_PENDING_FREEZE)
                .idCard(freeDepositQuery.getIdCard())
                .orderId(OrderIdUtil.generateBusinessOrderId(BusinessType.FREE_DEPOSIT, uid))
                .phone(freeDepositQuery.getPhoneNumber()).realName(freeDepositQuery.getRealName())
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .tenantId(TenantContextHolder.getTenantId()).transAmt(eleDepositOrder.getPayAmount().doubleValue())
                .type(FreeDepositOrder.TYPE_ZHIFUBAO).build();
        
        PxzCommonRequest<PxzFreeDepositOrderRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(freeDepositOrder.getOrderId());
        
        PxzFreeDepositOrderRequest request = new PxzFreeDepositOrderRequest();
        request.setPhone(freeDepositQuery.getPhoneNumber());
        request.setSubject("电池免押");
        request.setRealName(freeDepositQuery.getRealName());
        request.setIdNumber(freeDepositQuery.getIdCard());
        request.setTransAmt(
                BigDecimal.valueOf(freeDepositOrder.getTransAmt()).multiply(BigDecimal.valueOf(100)).intValue());
        query.setData(request);
    

        PxzCommonRsp<String> callPxzRsp = null;
        try {
            callPxzRsp = pxzDepositService.freeDepositOrder(query);
        } catch (Exception e) {
            log.error("Pxz ERROR! freeDepositOrder fail! uid={},orderId={}", uid, freeDepositOrder.getOrderId(), e);
            return Triple.of(false, "100401", "免押调用失败！");
        }
        
        if (Objects.isNull(callPxzRsp)) {
            log.error("Pxz ERROR! freeDepositOrder fail! rsp is null! uid={},orderId={}", uid,
                    freeDepositOrder.getOrderId());
            return Triple.of(false, "100401", "免押调用失败！");
        }
        
        if (!callPxzRsp.isSuccess()) {
            return Triple.of(false, "100401", callPxzRsp.getRespDesc());
        }
        
        insert(freeDepositOrder);
        eleDepositOrderService.insert(eleDepositOrder);
        
        //这里需要提前创建绑定表
        UserBatteryDeposit userBatteryDeposit  = new UserBatteryDeposit();
        userBatteryDeposit.setOrderId(userBatteryDeposit.getOrderId());
        userBatteryDeposit.setCreateTime(System.currentTimeMillis());
        userBatteryDeposit.setUid(uid);
        userBatteryDeposit.setUpdateTime(System.currentTimeMillis());
        userBatteryDeposit.setDid(freeDepositOrder.getId());
        userBatteryDeposit.setDelFlag(UserBatteryDeposit.DEL_NORMAL);
        userBatteryDeposit.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_FREE);
        userBatteryDepositService.insertOrUpdate(userBatteryDeposit);
        
        return Triple.of(true, null, callPxzRsp.getData());
    }
    
    @Override
    public Triple<Boolean, String, Object> freeDepositPreCheck() {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("FREE DEPOSIT ERROR! not found user info! uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        Triple<Boolean, String, Object> checkResult = checkUserCanFreeDeposit(uid, userInfo);
        if (Boolean.FALSE.equals(checkResult.getLeft())) {
            return checkResult;
        }
        
        FreeDepositUserInfoVo freeDepositUserInfoVo = new FreeDepositUserInfoVo();
        freeDepositUserInfoVo.setName(userInfo.getName());
        freeDepositUserInfoVo.setPhone(userInfo.getPhone());
        freeDepositUserInfoVo.setIdCard(userInfo.getIdNumber());
        
        return Triple.of(true, null, freeDepositUserInfoVo);
    }
    
    private Triple<Boolean, String, Object> checkUserCanFreeDeposit(Long uid, UserInfo userInfo) {
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(
                TenantContextHolder.getTenantId());
        if (electricityConfig.getFreeDepositType().equals(ElectricityConfig.FREE_DEPOSIT_TYPE_DEFAULT)) {
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
            return Triple.of(false, "ELECTRICITY.0049", "押金已经缴纳，无需重复缴纳");
        }
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> generateDepositOrder(UserInfo userInfo, FreeDepositQuery freeDepositQuery) {
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(freeDepositQuery.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("payDeposit  ERROR! not found Franchisee ！franchiseeId={},uid={}",
                    freeDepositQuery.getFranchiseeId(), userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0038", "未找到加盟商");
        }
        
        BigDecimal depositPayAmount = null;
        
        if (Objects.equals(franchisee.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
            depositPayAmount = franchisee.getBatteryDeposit();
        }
        
        //型号押金计算
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            if (Objects.isNull(freeDepositQuery.getModel())) {
                return Triple.of(false, "ELECTRICITY.0007", "不合法的参数");
            }
            
            //型号押金
            List<ModelBatteryDeposit> modelBatteryDepositList = JsonUtil.fromJsonArray(
                    franchisee.getModelBatteryDeposit(), ModelBatteryDeposit.class);
            if (ObjectUtil.isEmpty(modelBatteryDepositList)) {
                log.error("payDeposit  ERROR! not found modelBatteryDepositList ！franchiseeId={},uid={}",
                        freeDepositQuery.getFranchiseeId(), userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.00110", "未找到押金");
            }
            
            for (ModelBatteryDeposit modelBatteryDeposit : modelBatteryDepositList) {
                if ((double) (modelBatteryDeposit.getModel()) - freeDepositQuery.getModel() < 1
                        && (double) (modelBatteryDeposit.getModel()) - freeDepositQuery.getModel() >= 0) {
                    depositPayAmount = modelBatteryDeposit.getBatteryDeposit();
                    break;
                }
            }
        }
        
        if (Objects.isNull(depositPayAmount)) {
            log.error("payDeposit  ERROR! payAmount is null ！franchiseeId{},uid={}", freeDepositQuery.getFranchiseeId(),
                    userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.00110", "未找到押金");
        }
        
        //生成押金独立订单
        String depositOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT, userInfo.getUid());
        EleDepositOrder eleDepositOrder = EleDepositOrder.builder().orderId(depositOrderId).uid(userInfo.getUid())
                .phone(userInfo.getPhone()).name(userInfo.getName()).payAmount(depositPayAmount)
                .status(EleDepositOrder.STATUS_INIT).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).tenantId(userInfo.getTenantId())
                .franchiseeId(franchisee.getId()).payType(EleDepositOrder.FREE_DEPOSIT_PAYMENT).storeId(null)
                .modelType(franchisee.getModelType()).build();
        
        return Triple.of(true, null, eleDepositOrder);
    }
}
