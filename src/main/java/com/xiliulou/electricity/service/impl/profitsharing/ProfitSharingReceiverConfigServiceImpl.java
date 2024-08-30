/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/23
 */

package com.xiliulou.electricity.service.impl.profitsharing;

import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.MultiFranchiseeConstant;
import com.xiliulou.electricity.constant.profitsharing.ProfitSharingConfigConstant;
import com.xiliulou.electricity.converter.profitsharing.ProfitSharingReceiverConfigConverter;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingConfig;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingReceiverConfig;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingConfigReceiverStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingConfigReceiverTypeEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingReceiverConfigMapper;
import com.xiliulou.electricity.query.profitsharing.ProfitSharingReceiverConfigModel;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingReceiverConfigOptRequest;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingReceiverConfigQryRequest;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingReceiverConfigStatusOptRequest;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingConfigService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingReceiverConfigService;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.vo.profitsharing.ProfitSharingConfigVO;
import com.xiliulou.electricity.vo.profitsharing.ProfitSharingReceiverConfigDetailsVO;
import com.xiliulou.electricity.vo.profitsharing.ProfitSharingReceiverConfigVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/23 14:41
 */
@Service
public class ProfitSharingReceiverConfigServiceImpl implements ProfitSharingReceiverConfigService {
    
    @Resource
    private ProfitSharingReceiverConfigMapper profitSharingReceiverConfigMapper;
    
    @Resource
    private ProfitSharingConfigService profitSharingConfigService;
    
    @Resource
    private ElectricityPayParamsService electricityPayParamsService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private UserOauthBindService userOauthBindService;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Resource
    private OperateRecordUtil operateRecordUtil;
    
    @Slave
    @Override
    public List<ProfitSharingReceiverConfig> queryListByProfitSharingConfigId(Integer tenantId, Long profitSharingConfigId) {
        return profitSharingReceiverConfigMapper.selectListByTenantIdAndProfitSharingConfigId(tenantId, profitSharingConfigId);
    }
    
    @Slave
    @Override
    public List<ProfitSharingReceiverConfig> queryListByProfitSharingConfigIds(Integer tenantId, List<Long> profitSharingConfigIds) {
        return profitSharingReceiverConfigMapper.selectListByTenantIdAndProfitSharingConfigIds(tenantId, profitSharingConfigIds);
    }
    
    @Override
    public void insert(ProfitSharingReceiverConfigOptRequest request) {
        // 校验幂等
        this.checkIdempotent(request.getProfitSharingConfigId());
        
        Long profitSharingConfigId = request.getProfitSharingConfigId();
        ProfitSharingConfig profitSharingConfig = profitSharingConfigService.queryById(request.getTenantId(), profitSharingConfigId);
        if (Objects.isNull(profitSharingConfig)) {
            throw new BizException("分账方配置不存在");
        }
        
        //校验
        this.checkReceiver(request, profitSharingConfig, null);
        
        long time = System.currentTimeMillis();
        
        ProfitSharingReceiverConfig receiverConfig = ProfitSharingReceiverConfigConverter.optReqToEntity(request);
        
        receiverConfig.setFranchiseeId(profitSharingConfig.getFranchiseeId());
        receiverConfig.setReceiverStatus(ProfitSharingConfigReceiverStatusEnum.ENABLE.getCode());
        receiverConfig.setCreateTime(time);
        receiverConfig.setUpdateTime(time);
        profitSharingReceiverConfigMapper.insert(receiverConfig);
    }
    
    @Slave
    @Override
    public String queryWxMiniOpenIdByPhone(String phone, Integer tenantId) {
        User user = userService.queryByUserPhone(phone, User.TYPE_USER_NORMAL_WX_PRO, tenantId);
        
        if (Objects.isNull(user)) {
            return null;
        }
        
        Long uid = user.getUid();
        UserOauthBind userOauthBind = userOauthBindService.queryByUserPhone(uid, phone, UserOauthBind.SOURCE_WX_PRO, tenantId);
        if (Objects.isNull(userOauthBind)) {
            return null;
        }
        
        return userOauthBind.getThirdId();
    }
    
    @Override
    public void update(ProfitSharingReceiverConfigOptRequest request) {
        // 校验幂等
        this.checkIdempotent(request.getProfitSharingConfigId());
        
        Long id = request.getId();
        ProfitSharingReceiverConfig receiver = profitSharingReceiverConfigMapper.selectById(request.getTenantId(), id);
        if (Objects.isNull(receiver)) {
            throw new BizException("数据不存在");
        }
        
        if (!Objects.equals(request.getProfitSharingConfigId(), receiver.getProfitSharingConfigId())) {
            throw new BizException("数据不存在");
        }
        
        ProfitSharingConfig profitSharingConfig = profitSharingConfigService.queryById(request.getTenantId(), request.getProfitSharingConfigId());
        if (Objects.isNull(profitSharingConfig)) {
            throw new BizException("分账方配置不存在");
        }
        
        this.checkReceiver(request, profitSharingConfig, receiver);
        
        ProfitSharingReceiverConfig receiverConfig = ProfitSharingReceiverConfigConverter.optReqToEntity(request);
        
        receiverConfig.setUpdateTime(System.currentTimeMillis());
        
        profitSharingReceiverConfigMapper.update(receiverConfig);
        
        operateRecord(receiver, receiverConfig);
    }
    
    @Override
    public void updateStatus(ProfitSharingReceiverConfigStatusOptRequest request) {
        
        ProfitSharingReceiverConfig receiver = profitSharingReceiverConfigMapper.selectById(request.getTenantId(), request.getId());
        if (Objects.isNull(receiver)) {
            throw new BizException("数据不存在");
        }
        // 校验幂等
        this.checkIdempotent(receiver.getProfitSharingConfigId());
        
        ProfitSharingReceiverConfig receiverConfig = new ProfitSharingReceiverConfig();
        receiverConfig.setId(request.getId());
        receiverConfig.setUpdateTime(System.currentTimeMillis());
        receiverConfig.setReceiverStatus(receiver.getReceiverStatus());
        
        profitSharingReceiverConfigMapper.updateStatus(receiverConfig);
    }
    
    @Override
    public void removeById(Integer tenantId, Long id) {
        ProfitSharingReceiverConfig receiver = profitSharingReceiverConfigMapper.selectById(tenantId, id);
        if (Objects.isNull(receiver)) {
            throw new BizException("数据不存在");
        }
        // 校验幂等
        this.checkIdempotent(receiver.getProfitSharingConfigId());
        profitSharingReceiverConfigMapper.removeById(tenantId, id);
        operateDeleteRecord(receiver);
    }
    
    @Slave
    @Override
    public ProfitSharingReceiverConfigDetailsVO queryDetailsById(Integer tenantId, Long id) {
        
        ProfitSharingReceiverConfig receiver = profitSharingReceiverConfigMapper.selectById(tenantId, id);
        if (Objects.isNull(receiver)) {
            return null;
        }
        
        // 通过租户+加盟商查询可以走缓存
        ProfitSharingConfigVO profitSharingConfigVO = profitSharingConfigService.queryByTenantIdAndFranchiseeId(receiver.getTenantId(), receiver.getFranchiseeId());
        if (Objects.isNull(profitSharingConfigVO)) {
            return null;
        }
        ProfitSharingReceiverConfigDetailsVO detailsVO = ProfitSharingReceiverConfigConverter.qryEntityToDetailsVO(receiver);
        detailsVO.setWechatMerchantId(profitSharingConfigVO.getWechatMerchantId());
        return detailsVO;
    }
    
    @Slave
    @Override
    public List<ProfitSharingReceiverConfigVO> pageList(ProfitSharingReceiverConfigQryRequest request) {
        
        ProfitSharingReceiverConfigModel configModel = ProfitSharingReceiverConfigConverter.qryRequestToModel(request);
        
        List<ProfitSharingReceiverConfig> profitSharingReceiverConfigs = profitSharingReceiverConfigMapper.selectPage(configModel);
        
        List<ProfitSharingReceiverConfigVO> voList = ProfitSharingReceiverConfigConverter.qryEntityToVos(profitSharingReceiverConfigs);
        
        if (CollectionUtils.isEmpty(voList)) {
            return null;
        }
        
        List<Long> franchiseeIds = voList.stream().map(ProfitSharingReceiverConfigVO::getFranchiseeId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(franchiseeIds)) {
            return voList;
        }
        
        List<ProfitSharingConfigVO> profitSharingConfigVOS = profitSharingConfigService.queryListByTenantIdAndFranchiseeIds(configModel.getTenantId(), franchiseeIds);
        
        Map<Long, ProfitSharingConfigVO> sharingConfigVOMap = Optional.ofNullable(profitSharingConfigVOS).orElse(Collections.emptyList()).stream()
                .collect(Collectors.toMap(ProfitSharingConfigVO::getId, Function.identity()));
        
        franchiseeIds.remove(MultiFranchiseeConstant.DEFAULT_FRANCHISEE);
        List<Franchisee> franchisees = franchiseeService.queryByIds(franchiseeIds, request.getTenantId());
        Map<Long, String> franchiseeMap = Optional.ofNullable(franchisees).orElse(Collections.emptyList()).stream().collect(Collectors.toMap(Franchisee::getId, v -> v.getName()));
        voList.forEach(v -> {
            v.setFranchiseeName(franchiseeMap.get(v.getFranchiseeId()));
            ProfitSharingConfigVO profitSharingConfigVO = sharingConfigVOMap.get(v.getProfitSharingConfigId());
            if (Objects.nonNull(profitSharingConfigVO)) {
                v.setConfigType(profitSharingConfigVO.getConfigType());
                v.setWechatMerchantId(profitSharingConfigVO.getWechatMerchantId());
            }
        });
        return voList;
    }
    
    @Slave
    @Override
    public Integer count(ProfitSharingReceiverConfigQryRequest request) {
        ProfitSharingReceiverConfigModel configModel = ProfitSharingReceiverConfigConverter.qryRequestToModel(request);
        return profitSharingReceiverConfigMapper.count(configModel);
    }
    
    /**
     * 接收方信息校验
     *
     * @param request
     * @param profitSharingConfig
     * @author caobotao.cbt
     * @date 2024/8/26 09:01
     */
    private void checkReceiver(ProfitSharingReceiverConfigOptRequest request, ProfitSharingConfig profitSharingConfig, ProfitSharingReceiverConfig originalReceiverConfig) {
        
        List<ProfitSharingReceiverConfig> existList = profitSharingReceiverConfigMapper
                .selectListByTenantIdAndProfitSharingConfigId(request.getTenantId(), request.getProfitSharingConfigId());
        existList = Optional.ofNullable(existList).orElse(Collections.emptyList());
        
        // 个数校验（更新不校验）
        if (Objects.isNull(originalReceiverConfig) && existList.size() + 1 > ProfitSharingConfigConstant.MAX_RECEIVER_COUNT) {
            throw new BizException("最多可支持添加" + ProfitSharingConfigConstant.MAX_RECEIVER_COUNT + "个分账接收方");
        }
        
        //分账接收方比例
        BigDecimal totalScale = BigDecimal.ZERO;
        //已存在的分账账号
        List<String> existAccount = new ArrayList<>();
        
        for (ProfitSharingReceiverConfig receiverConfig : existList) {
            if (Objects.nonNull(originalReceiverConfig) && Objects.equals(receiverConfig.getId(), originalReceiverConfig.getId())) {
                // 修改时忽略本身
                continue;
            }
            
            totalScale = totalScale.add(receiverConfig.getScale());
            
            if (Objects.equals(request.getReceiverType(), receiverConfig.getReceiverType())) {
                existAccount.add(receiverConfig.getAccount());
            }
        }
        
        // 接收方比例超出限制
        if (profitSharingConfig.getScaleLimit().compareTo(totalScale.add(request.getScale())) < 0) {
            throw new BizException("分账接收方分账比例之和 必须小于等于 允许比例上限");
        }
        
        boolean repeat = existAccount.contains(request.getAccount());
        
        if (ProfitSharingConfigReceiverTypeEnum.MERCHANT.getCode().equals(request.getReceiverType())) {
            if (repeat) {
                throw new BizException("分账接收方（微信商户号）不可重复");
            }
            
            // 商户校验
            this.checkMerchantAccount(request);
            
        } else if (ProfitSharingConfigReceiverTypeEnum.PERSONAGE.getCode().equals(request.getReceiverType())) {
            if (repeat) {
                throw new BizException("分账接收方（openid）不可重复");
            }
            
            //个人账号校验
            this.checkPersonageAccount(request);
            
        } else {
            
            throw new BizException("未知的分账接收方类型");
        }
        
    }
    
    /**
     * 校验个人账号
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/8/26 10:14
     */
    private void checkPersonageAccount(ProfitSharingReceiverConfigOptRequest request) {
        UserOauthBind userOauthBind = userOauthBindService.queryOauthByOpenIdAndSource(request.getAccount(), UserOauthBind.SOURCE_WX_PRO, request.getTenantId());
        if (Objects.isNull(userOauthBind)) {
            throw new BizException("openid不存在");
        }
    }
    
    /**
     * 校验商户账号
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/8/26 10:14
     */
    private void checkMerchantAccount(ProfitSharingReceiverConfigOptRequest request) {
        String account = request.getAccount();
        ElectricityPayParams payParams = electricityPayParamsService.queryByWechatMerchantId(request.getTenantId(), account);
        if (Objects.nonNull(payParams)) {
            throw new BizException("不可与分账方（微信商户号）重复");
        }
    }
    
    
    /**
     * 幂等校验
     *
     * @param profitSharingConfigId
     * @author caobotao.cbt
     * @date 2024/8/23 08:54
     */
    private void checkIdempotent(Long profitSharingConfigId) {
        boolean b = redisService.setNx(String.format(CacheConstant.PROFIT_SHARING_RECEIVER_IDEMPOTENT_KEY, profitSharingConfigId), "1", 3000L, true);
        if (!b) {
            throw new BizException("频繁操作");
        }
    }
    
    private void operateRecord(ProfitSharingReceiverConfig old, ProfitSharingReceiverConfig newConfig) {
        
        String oldScale = old.getScale().multiply(new BigDecimal(100)) + "%";
        String newScale = newConfig.getScale().multiply(new BigDecimal(100)) + "%";
        
        Map<String, String> record = Maps.newHashMapWithExpectedSize(1);
        record.put("account", newConfig.getAccount() + "/" + newConfig.getReceiverName());
        record.put("scale", newScale);
        record.put("remark", newConfig.getRemark());
    
        Map<String, String> oldRecord = Maps.newHashMapWithExpectedSize(1);
        oldRecord.put("scale", oldScale);
        oldRecord.put("remark", old.getRemark());
        
        operateRecordUtil.record(oldRecord, record);
    }
    
    private void operateDeleteRecord(ProfitSharingReceiverConfig newConfig) {
        Map<String, String> record = Maps.newHashMapWithExpectedSize(1);
        record.put("account", newConfig.getAccount() + "/" + newConfig.getReceiverName());
        operateRecordUtil.record(null, record);
    }
}
