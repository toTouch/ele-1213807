package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.BankNoConstants;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.MultiFranchiseeConstant;
import com.xiliulou.electricity.entity.BankCard;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.PayTransferRecord;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserAmount;
import com.xiliulou.electricity.entity.UserAmountHistory;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.WechatWithdrawalCertificate;
import com.xiliulou.electricity.entity.WithdrawPassword;
import com.xiliulou.electricity.entity.WithdrawRecord;
import com.xiliulou.electricity.mapper.WithdrawRecordMapper;
import com.xiliulou.electricity.query.BatchHandleWithdrawRequest;
import com.xiliulou.electricity.query.CheckQuery;
import com.xiliulou.electricity.query.HandleWithdrawQuery;
import com.xiliulou.electricity.query.WithdrawQuery;
import com.xiliulou.electricity.query.WithdrawRecordQuery;
import com.xiliulou.electricity.query.WithdrawRecordQueryModel;
import com.xiliulou.electricity.service.BankCardService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.PayTransferRecordService;
import com.xiliulou.electricity.service.UserAmountHistoryService;
import com.xiliulou.electricity.service.UserAmountService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.WechatWithdrawalCertificateService;
import com.xiliulou.electricity.service.WithdrawPasswordService;
import com.xiliulou.electricity.service.WithdrawRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.WithdrawRecordVO;
import com.xiliulou.pay.weixin.query.PayTransferQuery;
import com.xiliulou.pay.weixin.transferPay.TransferPayHandlerService;
import com.xiliulou.security.authentication.console.CustomPasswordEncoder;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: Miss.Li
 * @Date: 2021/8/30 09:33
 * @Description:
 */
@Service("withdrawRecordService")
@Slf4j
public class WithdrawRecordRecordServiceImpl implements WithdrawRecordService {
    
    @Resource
    private WithdrawRecordMapper withdrawRecordMapper;
    
    @Autowired
    private UserAmountService userAmountService;
    
    @Autowired
    private BankCardService bankCardService;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    PayTransferRecordService payTransferRecordService;
    
    @Autowired
    TransferPayHandlerService transferPayHandlerService;
    
    @Autowired
    OperateRecordUtil operateRecordUtil;
    
    @Autowired
    UserService userService;
    
    @Autowired
    WithdrawPasswordService withdrawPasswordService;
    
    @Value("${security.encode.key:xiliu&lo@u%12345}")
    private String encodeKey;
    
    @Autowired
    CustomPasswordEncoder customPasswordEncoder;
    
    @Autowired
    UserAmountHistoryService userAmountHistoryService;
    
    @Autowired
    ElectricityConfigService electricityConfigService;
    
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    
    @Autowired
    WechatWithdrawalCertificateService wechatWithdrawalCertificateService;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R withdraw(WithdrawQuery query) {
        try {
            //根据用户类型查询用户余额
            BigDecimal balance;
            UserAmount userAmount = userAmountService.queryByUid(query.getUid());
            if (Objects.isNull(userAmount)) {
                log.warn("AMOUNT WARN! userAmount is null! uid={}", query.getUid());
                return R.fail("PAY_TRANSFER.0013", "未查询到用户账户");
            }
            balance = userAmount.getBalance();
            
            if (balance.compareTo(BigDecimal.valueOf(query.getAmount())) < 0) {
                log.warn("AMOUNT WARN! insufficient amount fault! uid={}, balance={}, requestAmount={}", query.getUid(), balance, query.getAmount());
                return R.fail("PAY_TRANSFER.0014", "提现余额不足");
            }
            if (query.getAmount() <= 2) {
                log.warn("AMOUNT WARN! less than the minimum amount fault! uid={}, balance={}, requestAmount={}", query.getUid(), balance, query.getAmount());
                return R.fail("PAY_TRANSFER.0015", "小于最低提现金额");
            }
            
            if (query.getAmount() > 20000) {
                log.warn("AMOUNT WARN! greater than the minimum amount fault! uid={}, balance={}, requestAmount={}", query.getUid(), balance, query.getAmount());
                return R.fail("PAY_TRANSFER.0016", "大于单次最大提现金额");
            }
            
            //查银行卡信息
            BankCard bankCard = bankCardService.getOne(Wrappers.<BankCard>lambdaQuery().eq(BankCard::getUid, query.getUid()).eq(BankCard::getEncBankNo, query.getBankNumber())
                    .eq(BankCard::getDelFlag, BankCard.DEL_NORMAL));
            if (Objects.isNull(bankCard)) {
                log.warn("BANKCARD WARN! bankCard is null fault! uid={}, bankNumber={}", query.getUid(), query.getBankNumber());
                return R.fail("PAY_TRANSFER.0017", "找不到此银行卡");
            }
            
            if (ObjectUtil.isEmpty(BankNoConstants.BankNoMap.get(bankCard.getEncBankCode()))) {
                log.warn("BANKCARD WARN! bankCard not support payment fault! uid={}, bankNumber={}", query.getUid(), query.getBankNumber());
                return R.fail("PAY_TRANSFER.0018", "不支持此银行卡转账");
            }
            
            BigDecimal handlingFee = BigDecimal.valueOf(this.getHandlingFee(query.getAmount().doubleValue()));
            BigDecimal amount = (BigDecimal.valueOf(query.getAmount()).subtract(handlingFee).setScale(2, BigDecimal.ROUND_HALF_UP));
            
            //插入提现表
            WithdrawRecord withdrawRecord = new WithdrawRecord();
            withdrawRecord.setUid(query.getUid());
            withdrawRecord.setTrueName(bankCard.getEncBindUserName());
            withdrawRecord.setBankCode(bankCard.getEncBankCode());
            withdrawRecord.setBankName((bankCard.getFullName()));
            withdrawRecord.setBankNumber(bankCard.getEncBankNo());
            withdrawRecord.setCreateTime(System.currentTimeMillis());
            withdrawRecord.setUpdateTime(System.currentTimeMillis());
            withdrawRecord.setStatus(WithdrawRecord.CHECKING);
            withdrawRecord.setOrderId(UUID.randomUUID().toString().replaceAll("-", ""));
            withdrawRecord.setHandlingFee(handlingFee.doubleValue());
            withdrawRecord.setAmount(amount.doubleValue());
            withdrawRecord.setTenantId(userAmount.getTenantId());
            withdrawRecordMapper.insert(withdrawRecord);
            
            //扣除余额
            
            userAmountService.updateReduceIncome(withdrawRecord.getUid(), withdrawRecord.getAmount() + withdrawRecord.getHandlingFee());
            
            UserAmountHistory history = UserAmountHistory.builder().type(UserAmountHistory.TYPE_WITHDRAW).createTime(System.currentTimeMillis())
                    .tenantId(TenantContextHolder.getTenantId()).amount(BigDecimal.valueOf(withdrawRecord.getAmount() + withdrawRecord.getHandlingFee()))
                    .oid(withdrawRecord.getId()).uid(withdrawRecord.getUid()).tenantId(userAmount.getTenantId()).build();
            userAmountHistoryService.insert(history);
            
            return R.ok();
            
        } finally {
            redisService.delete(CacheConstant.CACHE_WITHDRAW_USER_UID + query.getUid());
        }
        
    }
    
    @Slave
    @Override
    public R queryList(WithdrawRecordQuery withdrawRecordQuery) {
        List<WithdrawRecord> withdrawRecordList = withdrawRecordMapper.queryList(withdrawRecordQuery);
        List<WithdrawRecordVO> withdrawRecordVOs = new ArrayList<>();
        //脱敏
        for (WithdrawRecord withdrawRecord : withdrawRecordList) {
            
            WithdrawRecordVO withdrawRecordVO = new WithdrawRecordVO();
            BeanUtil.copyProperties(withdrawRecord, withdrawRecordVO);
            
            //查询身份证号
            BankCard bankCard = bankCardService.queryByUid(withdrawRecordVO.getUid());
            if (Objects.nonNull(bankCard)) {
                //				withdrawRecordVO.setIdNumber(DesensitizationUtil.idCard(bankCard.getEncBindIdNumber()));
                withdrawRecordVO.setIdNumber(bankCard.getEncBindIdNumber());
            }
            //			withdrawRecordVO.setBankNumber(DesensitizationUtil.bankCard(withdrawRecordVO.getBankNumber()));
            withdrawRecordVO.setBankNumber(withdrawRecordVO.getBankNumber());
            
            //设置请求提现金额
            BigDecimal amount = BigDecimal.valueOf(withdrawRecord.getAmount());
            BigDecimal handlingFee = BigDecimal.valueOf(withdrawRecord.getHandlingFee());
            BigDecimal requestAmount = amount.add(handlingFee).setScale(2, BigDecimal.ROUND_HALF_UP);
            withdrawRecordVO.setRequestAmount(requestAmount.doubleValue());
            
            //查询用户名称
            User user = userService.queryByUidFromCache(withdrawRecordVO.getUid());
            if (Objects.nonNull(user)) {
                withdrawRecordVO.setPhone(user.getPhone());
            }
            if (!Objects.isNull(withdrawRecord.getAuditorId())) {
                User auditor = userService.queryByUidFromCache(withdrawRecord.getAuditorId());
                if (!Objects.isNull(auditor)) {
                    withdrawRecordVO.setAuditorName(auditor.getName());
                }
            }
            
            // 查询加盟商名称
            if (Objects.nonNull(withdrawRecord.getFranchiseeId())) {
                Franchisee franchisee = franchiseeService.queryByIdFromCache(withdrawRecord.getFranchiseeId());
                if (Objects.nonNull(franchisee)) {
                    withdrawRecordVO.setFranchiseeName(franchisee.getName());
                }
            }
            
            withdrawRecordVOs.add(withdrawRecordVO);
            
        }
        return R.ok(withdrawRecordVOs);
    }
    
    @Slave
    @Override
    public R queryCount(WithdrawRecordQuery withdrawRecordQuery) {
        return R.ok(withdrawRecordMapper.queryCount(withdrawRecordQuery));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R handleWithdraw(HandleWithdrawQuery handleWithdrawQuery) {
        
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        //提现密码确认
        WithdrawPassword withdrawPassword = withdrawPasswordService.queryFromCache(tenantId);
        if (Objects.isNull(withdrawPassword)) {
            return R.fail("请设置提现密码");
        }
        
        String decryptPassword = null;
        String encryptPassword = handleWithdrawQuery.getPassword();
        if (StrUtil.isNotEmpty(encryptPassword)) {
            //解密密码
            decryptPassword = decryptPassword(encryptPassword);
            if (StrUtil.isEmpty(decryptPassword)) {
                log.warn("UPDATE WITHDRAW PASSWORD WARN! decryptPassword fault! password={}", withdrawPassword.getPassword());
                return R.fail("系统错误!");
            }
        }
        
        if (!customPasswordEncoder.matches(decryptPassword, withdrawPassword.getPassword())) {
            log.warn("UPDATE WITHDRAW PASSWORD WARN! password is not equals fault! password={}, withdrawPassword={}", decryptPassword, withdrawPassword);
            return R.fail("提现密码错误");
        }
        
        if (!Objects.equals(handleWithdrawQuery.getStatus(), WithdrawRecord.CHECK_REFUSE) && !Objects.equals(handleWithdrawQuery.getStatus(), WithdrawRecord.CHECK_PASS)) {
            log.warn("UPDATE WITHDRAW PASSWORD WARN! Illegal parameter! statusNumber={}", handleWithdrawQuery.getStatus());
            return R.fail("参数不合法");
        }
        WithdrawRecord withdrawRecord = withdrawRecordMapper.selectById(handleWithdrawQuery.getId());
        if (!Objects.equals(withdrawRecord.getStatus(), WithdrawRecord.CHECKING)) {
            log.warn("UPDATE WITHDRAW PASSWORD WARN! repeat audit fault! statusNumber={}", withdrawRecord.getStatus());
            return R.fail("不能重复审核");
        }
    
        UserInfo userInfo = userInfoService.queryByUidFromCache(withdrawRecord.getUid());
        if (ObjectUtils.isNotEmpty(handleWithdrawQuery.getBindFranchiseeIdList()) && Objects.nonNull(userInfo)
                && !handleWithdrawQuery.getBindFranchiseeIdList().contains(userInfo.getFranchiseeId())) {
            return R.fail("120240", "当前加盟商无权限操作");
        }
    
        withdrawRecord.setStatus(handleWithdrawQuery.getStatus());
        withdrawRecord.setUpdateTime(System.currentTimeMillis());
        withdrawRecord.setCheckTime(System.currentTimeMillis());
        withdrawRecord.setAuditorId(user.getUid());
        
        //提现审核拒绝
        if (Objects.equals(handleWithdrawQuery.getStatus(), WithdrawRecord.CHECK_REFUSE)) {
            withdrawRecord.setMsg(handleWithdrawQuery.getMsg());
            withdrawRecordMapper.updateById(withdrawRecord);
            
            //回退余额
            
            UserAmount userAmount = userAmountService.queryByUid(withdrawRecord.getUid());
            if (Objects.nonNull(userAmount)) {
                userAmountService.updateRollBackIncome(withdrawRecord.getUid(), withdrawRecord.getAmount() + withdrawRecord.getHandlingFee());
                
                UserAmountHistory history = UserAmountHistory.builder().type(UserAmountHistory.TYPE_WITHDRAW_ROLLBACK).createTime(System.currentTimeMillis())
                        .tenantId(TenantContextHolder.getTenantId()).amount(BigDecimal.valueOf(withdrawRecord.getAmount() + withdrawRecord.getHandlingFee()))
                        .oid(withdrawRecord.getId()).uid(withdrawRecord.getUid()).tenantId(userAmount.getTenantId()).build();
                userAmountHistoryService.insert(history);
            }
            operateRecordUtil.record(null, withdrawRecord);
            return R.ok();
        }
        
        //线下提现
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
        if (Objects.nonNull(electricityConfig)) {
            if (Objects.equals(electricityConfig.getIsWithdraw(), ElectricityConfig.NON_WITHDRAW)) {
                //修改提现表
                withdrawRecord.setStatus(WithdrawRecord.WITHDRAWING_SUCCESS);
                withdrawRecord.setType(WithdrawRecord.TYPE_UN_ONLINE);
                //提现审核通过
                withdrawRecordMapper.updateById(withdrawRecord);
                operateRecordUtil.record(null, withdrawRecord);
                return R.ok();
            }
        }
        
        //线上提现
        withdrawRecord.setType(WithdrawRecord.TYPE_ONLINE);
        withdrawRecordMapper.updateById(withdrawRecord);
        operateRecordUtil.record(null, withdrawRecord);
        return transferPay(withdrawRecord);
        
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateStatus(String orderId, Double amount, Double handlingFee, Boolean result, String arriveTime) {
        WithdrawRecord withdrawRecord = withdrawRecordMapper.selectOne(new LambdaQueryWrapper<WithdrawRecord>().eq(WithdrawRecord::getOrderId, orderId));
        if (Objects.isNull(withdrawRecord)) {
            log.warn("withdrawRecord  is null! orderId:{}", orderId);
            return R.fail("查不到该笔流水");
        }
        
        UserAmount userAmount = null;
        
        if (!result) {
            //回退余额
            
            userAmount = userAmountService.queryByUid(withdrawRecord.getUid());
            if (Objects.nonNull(userAmount)) {
                userAmountService.updateRollBackIncome(withdrawRecord.getUid(), withdrawRecord.getAmount() + withdrawRecord.getHandlingFee());
                
                UserAmountHistory history = UserAmountHistory.builder().type(UserAmountHistory.TYPE_WITHDRAW_ROLLBACK).createTime(System.currentTimeMillis())
                        .tenantId(TenantContextHolder.getTenantId()).amount(BigDecimal.valueOf(withdrawRecord.getAmount() + withdrawRecord.getHandlingFee()))
                        .oid(withdrawRecord.getId()).uid(withdrawRecord.getUid()).tenantId(userAmount.getTenantId()).build();
                userAmountHistoryService.insert(history);
            }
            
        }
        
        if (result) {
            //修改提现表
            withdrawRecord.setStatus(WithdrawRecord.WITHDRAWING_SUCCESS);
            withdrawRecord.setHandlingFee(handlingFee);
            withdrawRecord.setUpdateTime(System.currentTimeMillis());
            withdrawRecord.setAmount(amount);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //设置要读取的时间字符串格式
            try {
                Date date = format.parse(arriveTime);
                //转换为Date类
                Long timestamp = date.getTime();
                withdrawRecord.setArriveTime(timestamp);
                withdrawRecordMapper.updateById(withdrawRecord);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return R.ok();
        } else {
            //修改提现表
            withdrawRecord.setStatus(WithdrawRecord.WITHDRAWING_FAIL);
            withdrawRecord.setUpdateTime(System.currentTimeMillis());
            withdrawRecordMapper.updateById(withdrawRecord);
            return R.ok();
        }
        
    }
    
    @Override
    public R check(CheckQuery query) {
        BankCard bankCard = bankCardService.queryByUidAndDel(query.getUid());
        if (Objects.isNull(bankCard)) {
            log.warn("user is not bind card! uid:{}", query.getUid());
            return R.fail("PAY_TRANSFER.0011", "您未绑定银行卡，不能提现");
        }
        
        //校验姓名
        if (!Objects.equals(bankCard.getEncBindUserName(), query.getName())) {
            log.warn("phone is not equal! uName:{} ,qName:{},uid:{}", bankCard.getEncBindUserName(), query.getName(), query.getUid());
            return R.fail("PAY_TRANSFER.0010", "姓名与绑定人不符");
        }
        
        if (!bankCard.getEncBindIdNumber().contains(query.getIdNumber())) {
            log.warn("idNumber is not equal! uIdNumber:{} ,qIdNumber:{},uid:{}", bankCard.getEncBindIdNumber(), query.getIdNumber(), query.getUid());
            return R.fail("PAY_TRANSFER.0012", "身份证后四位与绑定的不一致");
        }
        
        return R.ok();
    }
    
    @Override
    public WithdrawRecord selectById(Long oid) {
        return withdrawRecordMapper.selectById(oid);
    }
    
    @Override
    public R getWithdrawCount(Long uid) {
        return R.ok(
                withdrawRecordMapper.selectCount(new LambdaQueryWrapper<WithdrawRecord>().eq(WithdrawRecord::getStatus, WithdrawRecord.CHECKING).eq(WithdrawRecord::getUid, uid)));
    }
    
    /**
     * 批量提现审核 线下处理的金额 无需再调用接口进行转账 只需将对应的订单的状态修改为通过或者拒绝即可
     *
     * @param batchHandleWithdrawRequest
     * @return
     */
    @Override
    @Transactional
    public R batchHandleWithdraw(BatchHandleWithdrawRequest batchHandleWithdrawRequest) {
        
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!redisService.setNx(CacheConstant.CACHE_BATCH_WITHDRAW_OPER_USER_LOCK + user.getUid(), "1", 3 * 1000L, false)) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        //提现密码确认
        WithdrawPassword withdrawPassword = withdrawPasswordService.queryFromCache(tenantId);
        if (Objects.isNull(withdrawPassword)) {
            return R.fail("100421", "请设置提现密码");
        }
        
        String decryptPassword = null;
        String encryptPassword = batchHandleWithdrawRequest.getPassword();
        if (StrUtil.isNotEmpty(encryptPassword)) {
            //解密密码
            decryptPassword = decryptPassword(encryptPassword);
            if (StrUtil.isEmpty(decryptPassword)) {
                log.warn("UPDATE WITHDRAW PASSWORD WARN! decryptPassword fault! password={}", withdrawPassword.getPassword());
                return R.fail("SYSTEM.0001", "系统错误!");
            }
        }
        
        if (!customPasswordEncoder.matches(decryptPassword, withdrawPassword.getPassword())) {
            log.warn("UPDATE WITHDRAW PASSWORD WARN! password is not equals fault! password={}, withdrawPassword={}", decryptPassword, withdrawPassword);
            return R.fail("100420", "提现密码错误");
        }
        
        if (!Objects.equals(batchHandleWithdrawRequest.getStatus(), WithdrawRecord.CHECK_REFUSE) && !Objects.equals(batchHandleWithdrawRequest.getStatus(),
                WithdrawRecord.CHECK_PASS)) {
            log.warn("UPDATE WITHDRAW PASSWORD WARN! Illegal parameter! statusNumber={}", batchHandleWithdrawRequest.getStatus());
            return R.fail("100423", "错误的审批操作");
        }
        
        // 判断当前租户是否允许线下提现
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
        if (Objects.nonNull(electricityConfig) && !Objects.equals(electricityConfig.getIsWithdraw(), ElectricityConfig.NON_WITHDRAW)) {
            log.warn("UPDATE WITHDRAW PASSWORD WARN! Illegal parameter! statusNumber={}", batchHandleWithdrawRequest.getStatus());
            return R.fail("100422", "系统设置中-提现方式（线下）未生效");
        }
        
        WithdrawRecordQueryModel withdrawRecordQueryModel = WithdrawRecordQueryModel.builder().tenantId(tenantId).idList(batchHandleWithdrawRequest.getIdList()).build();
        List<WithdrawRecord> withdrawRecordList = withdrawRecordMapper.selectList(withdrawRecordQueryModel);
        if (CollectionUtils.isEmpty(withdrawRecordList) || !Objects.equals(withdrawRecordList.size(), batchHandleWithdrawRequest.getIdList().size())) {
            log.warn("batch handle withdraw record is not exists! idList={}", batchHandleWithdrawRequest.getIdList());
            return R.fail("100419", "提现记录不存在。");
        }
        
        if (ObjectUtils.isNotEmpty(batchHandleWithdrawRequest.getBindFranchiseeIdList())) {
            List<Long> franchiseeIdList = withdrawRecordList.stream().map(WithdrawRecord::getFranchiseeId)
                    .filter(franchiseeId -> Objects.nonNull(franchiseeId) && !batchHandleWithdrawRequest.getBindFranchiseeIdList().contains(franchiseeId)).distinct()
                    .collect(Collectors.toList());
            
            if (ObjectUtils.isNotEmpty(franchiseeIdList)) {
                return R.fail("120240", "当前加盟商无权限操作");
            }
        }
        
        // 过滤已经审核的提现订单
        List<WithdrawRecord> alreadyAudit = new ArrayList<>();
        
        Set<Long> uidList = new HashSet<>();
        
        withdrawRecordList.forEach(withdrawRecord -> {
            if (!Objects.equals(withdrawRecord.getStatus(), WithdrawRecord.CHECKING)) {
                alreadyAudit.add(withdrawRecord);
            }
            uidList.add(withdrawRecord.getUid());
        });
        
        if (!CollectionUtils.isEmpty(alreadyAudit)) {
            return R.fail("100419", "网络不佳，请刷新重试操作。");
        }
        
        WithdrawRecord withdrawRecord = new WithdrawRecord();
        // 设置状态
        withdrawRecord.setUpdateTime(System.currentTimeMillis());
        withdrawRecord.setCheckTime(System.currentTimeMillis());
        withdrawRecord.setAuditorId(user.getUid());
        
        // 提现审核拒绝
        if (Objects.equals(batchHandleWithdrawRequest.getStatus(), WithdrawRecord.CHECK_REFUSE)) {
            withdrawRecord.setMsg(batchHandleWithdrawRequest.getMsg());
            withdrawRecord.setStatus(WithdrawRecord.CHECK_REFUSE);
            
            List<UserAmount> userAmountList = userAmountService.queryListByUidList(uidList, tenantId);
            if (!CollectionUtils.isEmpty(userAmountList)) {
                Map<Long, UserAmount> userAmountMap = userAmountList.stream().collect(Collectors.toMap(UserAmount::getUid, Function.identity()));
                withdrawRecordList.forEach(withdrawRecordTemp -> {
                    //回退余额
                    UserAmount userAmount = userAmountMap.get(withdrawRecordTemp.getUid());
                    if (Objects.nonNull(userAmount)) {
                        userAmountService.updateRollBackIncome(withdrawRecordTemp.getUid(), withdrawRecordTemp.getAmount() + withdrawRecordTemp.getHandlingFee());
                        
                        UserAmountHistory history = UserAmountHistory.builder().type(UserAmountHistory.TYPE_WITHDRAW_ROLLBACK).createTime(System.currentTimeMillis())
                                .amount(BigDecimal.valueOf(withdrawRecordTemp.getAmount() + withdrawRecordTemp.getHandlingFee())).oid(withdrawRecordTemp.getId())
                                .uid(withdrawRecordTemp.getUid()).tenantId(userAmount.getTenantId()).build();
                        userAmountHistoryService.insert(history);
                    }
                });
            }
            
        } else {
            // 审核通过
            //线下提现
            withdrawRecord.setType(WithdrawRecord.TYPE_UN_ONLINE);
            // 提现成功
            withdrawRecord.setStatus(WithdrawRecord.WITHDRAWING_SUCCESS);
        }
        
        // 批量修改审核结果
        withdrawRecordMapper.batchWithdrawById(withdrawRecord, batchHandleWithdrawRequest.getIdList(), tenantId);
        Map<Object, Object> build = MapUtil.builder().put("data", withdrawRecord).build();
        if (!CollectionUtil.isEmpty(batchHandleWithdrawRequest.getIdList())) {
            build.put("count", batchHandleWithdrawRequest.getIdList().size());
        }
        operateRecordUtil.record(null, build);
        return R.ok();
    }
    
    @Transactional(rollbackFor = Exception.class)
    public R transferPay(WithdrawRecord withdrawRecord) {
        
        // 和产品沟通，余额提现，加盟商ID传入默认 0
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryPreciseCacheByTenantIdAndFranchiseeId(withdrawRecord.getTenantId(),
                MultiFranchiseeConstant.DEFAULT_FRANCHISEE);
        
        if (Objects.isNull(electricityPayParams)) {
            throw new AuthenticationServiceException("未能查找到appId和appSecret！");
        }
        
        WechatWithdrawalCertificate certificate = wechatWithdrawalCertificateService.queryByTenantIdAndFranchiseeId(withdrawRecord.getTenantId(),
                MultiFranchiseeConstant.DEFAULT_FRANCHISEE);
        if (Objects.isNull(certificate) || certificate.getCertificateValue().length == 0) {
            throw new AuthenticationServiceException("未能查找到appId和appSecret！");
        }
        
        Double amount = BigDecimal.valueOf(withdrawRecord.getAmount()).multiply(BigDecimal.valueOf(100)).doubleValue();
        
        //微信提现中
        PayTransferRecord payTransferRecord = PayTransferRecord.builder().channelMchId(electricityPayParams.getWechatMerchantId())
                .channelMchAppId(electricityPayParams.getMerchantMinProAppId()).description(String.valueOf(System.currentTimeMillis())).encTrueName(withdrawRecord.getTrueName())
                .bankNo(withdrawRecord.getBankCode()).encBankNo(withdrawRecord.getBankNumber()).orderId(withdrawRecord.getOrderId()).requestAmount(amount.longValue())
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).uid(withdrawRecord.getUid())
                .transactionOrderNo(generateTransactionId(withdrawRecord.getId(), withdrawRecord.getUid())).tenantId(withdrawRecord.getTenantId())
                .payFranchiseeId(electricityPayParams.getFranchiseeId()).wechatMerchantId(electricityPayParams.getWechatMerchantId()).build();
        payTransferRecordService.insert(payTransferRecord);
        
        PayTransferQuery payTransferQuery = PayTransferQuery.builder().mchId(electricityPayParams.getWechatMerchantId()).partnerOrderNo(payTransferRecord.getOrderId())
                .amount(payTransferRecord.getRequestAmount()).encBankNo(withdrawRecord.getBankNumber()).encTrueName(payTransferRecord.getEncTrueName())
                .bankNo(payTransferRecord.getBankNo()).description(payTransferRecord.getDescription()).appId(electricityPayParams.getMerchantMinProAppId())
                .patternedKey(electricityPayParams.getPaternerKey()).certificateBinary(certificate.getCertificateValue()).build();
        
        Pair<Boolean, Object> transferPayPair = transferPayHandlerService.transferPay(payTransferQuery);
        
        //提现外部记录
        PayTransferRecord updatePayTransferRecord = new PayTransferRecord();
        updatePayTransferRecord.setId(payTransferRecord.getId());
        updatePayTransferRecord.setUpdateTime(System.currentTimeMillis());
        
        //提现记录
        WithdrawRecord updateWithdrawRecord = new WithdrawRecord();
        updateWithdrawRecord.setId(withdrawRecord.getId());
        updateWithdrawRecord.setUpdateTime(System.currentTimeMillis());
        
        //提现未提交
        if (!transferPayPair.getLeft()) {
            
            updatePayTransferRecord.setErrorMsg("签名错误");
            updatePayTransferRecord.setErrorCode("签名错误");
            updatePayTransferRecord.setStatus(PayTransferRecord.STATUS_FAILED);
            payTransferRecordService.update(updatePayTransferRecord);
            
            updateWithdrawRecord.setMsg("签名错误");
            updateWithdrawRecord.setStatus(WithdrawRecord.WITHDRAWING_FAIL);
            withdrawRecordMapper.updateById(updateWithdrawRecord);
            
            rollBackWithdraw(withdrawRecord);
            return R.ok();
        }
        
        Map<String, String> resultMap = (Map) transferPayPair.getRight();
        
        //提现提交失败
        if (ObjectUtil.equal(PayTransferRecord.TRANSFER_RESULT_FAIL, resultMap.get("return_code"))) {
            String errmsg = resultMap.get("return_msg");
            if (StringUtils.isEmpty(errmsg)) {
                errmsg = "未知错误!";
            }
            String errCode = resultMap.get("result_code");
            
            updatePayTransferRecord.setErrorCode(ObjectUtil.isEmpty(errCode) ? "未知错误码" : errCode);
            updatePayTransferRecord.setErrorMsg(errmsg);
            updatePayTransferRecord.setStatus(PayTransferRecord.STATUS_FAILED);
            payTransferRecordService.update(updatePayTransferRecord);
            
            updateWithdrawRecord.setMsg(errmsg);
            updateWithdrawRecord.setStatus(WithdrawRecord.WITHDRAWING_FAIL);
            withdrawRecordMapper.updateById(updateWithdrawRecord);
            
            rollBackWithdraw(withdrawRecord);
            return R.ok();
        }
        
        //提现提交成功
        if (ObjectUtil.equal(PayTransferRecord.TRANSFER_RESULT_SUCCESS, resultMap.get("return_code"))) {
            
            //业务结果  成功
            if (ObjectUtil.equal(PayTransferRecord.TRANSFER_RESULT_SUCCESS, resultMap.get("result_code"))) {
                
                updatePayTransferRecord.setStatus(PayTransferRecord.STATUS_TRANSFER_ING);
                payTransferRecordService.update(updatePayTransferRecord);
                
                updateWithdrawRecord.setStatus(WithdrawRecord.WITHDRAWING);
                withdrawRecordMapper.updateById(updateWithdrawRecord);
                return R.ok();
            }
            
            //业务结果  失败
            if (ObjectUtil.equal(PayTransferRecord.TRANSFER_RESULT_FAIL, resultMap.get("result_code"))) {
                String errmsg = resultMap.get("err_code_des");
                if (StringUtils.isEmpty(errmsg)) {
                    errmsg = "未知错误!";
                }
                String errCode = resultMap.get("err_code");
                updatePayTransferRecord.setErrorCode(ObjectUtil.isEmpty(errCode) ? "未知错误码" : errCode);
                updatePayTransferRecord.setErrorMsg(errmsg);
                updatePayTransferRecord.setStatus(PayTransferRecord.STATUS_FAILED);
                payTransferRecordService.update(updatePayTransferRecord);
                
                updateWithdrawRecord.setMsg(errmsg);
                updateWithdrawRecord.setStatus(WithdrawRecord.WITHDRAWING_FAIL);
                withdrawRecordMapper.updateById(updateWithdrawRecord);
                
                rollBackWithdraw(withdrawRecord);
                return R.ok();
            }
        }
        
        return R.ok();
    }
    
    public Double getHandlingFee(Double amount) {
        BigDecimal handlingFee = BigDecimal.valueOf(25);
        if (amount <= 1000) {
            handlingFee = BigDecimal.valueOf(1);
        }
        if (amount > 1000 && amount <= 25000) {
            handlingFee = BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(1.001), new MathContext(2, RoundingMode.CEILING)).multiply(BigDecimal.valueOf(0.001));
        }
        return Double.valueOf(handlingFee.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
    }
    
    private String generateTransactionId(Long id, Long uid) {
        return String.valueOf(System.currentTimeMillis()) + id + uid + RandomUtil.randomNumbers(2);
    }
    
    private void rollBackWithdraw(WithdrawRecord withdrawRecord) {
        
        UserAmount userAmount = userAmountService.queryByUid(withdrawRecord.getUid());
        if (Objects.nonNull(userAmount)) {
            userAmountService.updateRollBackIncome(withdrawRecord.getUid(), withdrawRecord.getAmount() + withdrawRecord.getHandlingFee());
            
            UserAmountHistory history = UserAmountHistory.builder().type(UserAmountHistory.TYPE_WITHDRAW_ROLLBACK).createTime(System.currentTimeMillis())
                    .tenantId(TenantContextHolder.getTenantId()).amount(BigDecimal.valueOf(withdrawRecord.getAmount() + withdrawRecord.getHandlingFee()))
                    .oid(withdrawRecord.getId()).uid(withdrawRecord.getUid()).tenantId(userAmount.getTenantId()).build();
            userAmountHistoryService.insert(history);
        }
        
    }
    
    private String decryptPassword(String encryptPassword) {
        AES aes = new AES(Mode.CBC, Padding.ZeroPadding, new SecretKeySpec(encodeKey.getBytes(), "AES"), new IvParameterSpec(encodeKey.getBytes()));
        
        return new String(aes.decrypt(Base64.decode(encryptPassword.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
    }
    
}
