package com.xiliulou.electricity.service.impl.merchant;

import cn.hutool.core.util.IdUtil;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.electricity.bo.merchant.MerchantWithdrawSendBO;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.config.merchant.MerchantConfig;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.merchant.MerchantWithdrawApplicationRecordConstant;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.enums.merchant.MerchantWithdrawTypeEnum;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.electricity.service.merchant.MerchantWithdrawApplicationBizService;
import com.xiliulou.electricity.service.merchant.MerchantWithdrawApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MerchantWithdrawApplicationBizServiceImpl implements MerchantWithdrawApplicationBizService {
    @Resource
    private MerchantWithdrawApplicationService merchantWithdrawApplicationService;

    @Resource
    private MerchantConfig merchantConfig;

    @Resource
    private UserOauthBindService userOauthBindService;

    @Resource
    private TenantService tenantService;

    @Resource
    private WechatPayParamsBizService wechatPayParamsBizService;

    @Override
    public void handleSendMerchantWithdrawProcess(Integer tenantId) {
        MDC.put(CommonConstant.TRACE_ID, IdUtil.fastSimpleUUID());

        if (Objects.isNull(merchantConfig) || Objects.isNull(merchantConfig.getMerchantAppletId()) ||
                Objects.isNull(merchantConfig.getMerchantAppletSecret())) {
            log.error("merchant withdraw send task error! merchant config is null");
            return;
        }

        Integer size = 200;
        Integer startId = 0;

        // 全量处理
        while (true) {
            List<Integer> tenantIdList = tenantService.queryIdListByStartId(startId, size);
            if (ObjectUtils.isEmpty(tenantIdList)) {
                return;
            }

            startId = tenantIdList.get(tenantIdList.size() - 1);

            tenantIdList.stream().forEach(id -> {
                handleSendByTenantId(id);
            });
        }
    }

    private void handleSendByTenantId(Integer tenantId) {
        Long size = 200L;
        Long startId = 0L;

        while (true) {
            List<MerchantWithdrawSendBO> merchantWithdrawApplicationList = merchantWithdrawApplicationService.listAuditSuccess(tenantId, size, startId, MerchantWithdrawTypeEnum.NEW.getCode());
            if (ObjectUtils.isEmpty(merchantWithdrawApplicationList)) {
                break;
            }

            startId = merchantWithdrawApplicationList.get(merchantWithdrawApplicationList.size() - 1).getApplicationId();

            List<Long> uidList = merchantWithdrawApplicationList.stream().map(MerchantWithdrawSendBO::getUid).distinct().collect(Collectors.toList());

            List<UserOauthBind> userOauthBindList = userOauthBindService.listByUidAndTenantAndSource(uidList, tenantId, UserOauthBind.SOURCE_WX_PRO);
            Map<Long, String> uidBindMap = new HashMap<>();
            if (ObjectUtils.isNotEmpty(userOauthBindList)) {
                uidBindMap = userOauthBindList.stream().collect(Collectors.toMap(UserOauthBind::getUid, UserOauthBind::getThirdId, (v1, v2) -> v1));
            }

            // 根据加盟商进行分组
            Map<Long, List<MerchantWithdrawSendBO>> franchiseeIdGroupMap = merchantWithdrawApplicationList.stream().collect(Collectors.groupingBy(MerchantWithdrawSendBO::getFranchiseeId));
            Map<Long, String> finalUidBindMap = uidBindMap;
            franchiseeIdGroupMap.forEach((franchiseeId, v) -> {
                // 检测支付配置是否存在
                WechatPayParamsDetails wechatPayParamsDetails = null;
                try {
                    wechatPayParamsDetails = wechatPayParamsBizService.getDetailsByIdTenantIdAndFranchiseeId(tenantId, franchiseeId);
                } catch (Exception e) {
                    log.error("merchant withdraw send task error!, get wechat pay params details error, tenantId = {}, franchiseeId={}", tenantId, franchiseeId, e);
                    return;
                }

                if (Objects.isNull(wechatPayParamsDetails) || Objects.isNull(wechatPayParamsDetails.getFranchiseeId())) {
                    log.error("merchant withdraw send task error! wechat pay params details is null, tenantId = {}, franchiseeId = {}", tenantId, franchiseeId);
                    return;
                }

                WechatPayParamsDetails finalWechatPayParamsDetails = wechatPayParamsDetails;
                // 支付配置类型
                Integer payConfigType;
                if (!Objects.equals(wechatPayParamsDetails.getFranchiseeId(), NumberConstant.ZERO_L)) {
                    payConfigType = MerchantWithdrawApplicationRecordConstant.PAY_CONFIG_TYPE_FRANCHISEE;
                } else {
                    payConfigType = MerchantWithdrawApplicationRecordConstant.PAY_CONFIG_TYPE_DEFAULT;
                }

                v.stream().forEach(item -> {
                    Triple<Boolean, String, Object> triple = merchantWithdrawApplicationService.sendTransfer(item, finalUidBindMap.get(item.getUid()), finalWechatPayParamsDetails, payConfigType);
                    if (!triple.getLeft()) {
                        log.error("merchant withdraw send task error! ");
                    }
                });
            });
        }
    }
}
