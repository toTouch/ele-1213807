package com.xiliulou.electricity.service.impl.merchant;

import cn.hutool.core.date.DateUtil;
import com.alipay.api.AlipayApiException;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.wp.entity.AppTemplateQuery;
import com.xiliulou.core.wp.service.WeChatAppTemplateService;
import com.xiliulou.electricity.bo.merchant.MerchantWithdrawSendBO;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.config.merchant.MerchantConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.DateFormatConstant;
import com.xiliulou.electricity.constant.MultiFranchiseeConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.merchant.MerchantWithdrawApplicationConstant;
import com.xiliulou.electricity.constant.merchant.MerchantWithdrawApplicationRecordConstant;
import com.xiliulou.electricity.constant.merchant.MerchantWithdrawConstant;
import com.xiliulou.electricity.converter.ElectricityPayParamsConverter;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.enums.merchant.MerchantWithdrawApplicationStateEnum;
import com.xiliulou.electricity.enums.merchant.MerchantWithdrawTypeEnum;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.service.merchant.MerchantWithdrawApplicationBizService;
import com.xiliulou.electricity.service.merchant.MerchantWithdrawApplicationService;
import com.xiliulou.pay.weixinv3.dto.WechatTransferOrderQueryResultV2;
import com.xiliulou.pay.weixinv3.v2.query.WechatTransferOrderRecordRequestV2;
import com.xiliulou.pay.weixinv3.v2.query.WechatV3CommonRequest;
import com.xiliulou.pay.weixinv3.v2.service.WechatV3TransferInvokeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;
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

    @Resource
    private MerchantService merchantService;

    @Resource
    private WechatV3TransferInvokeService wechatV3TransferInvokeService;

    @Resource
    private RedisService redisService;

    @Resource
    private WeChatAppTemplateService weChatAppTemplateService;

    @Override
    public void handleSendMerchantWithdrawProcess(Integer tenantId) {

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

    @Override
    public void handleQueryWithdrawResult(Integer tenantId) {
        int offset = 0;
        int size = 200;

        while (true) {
            List<Merchant> merchantList = merchantService.list(offset, size);
            if (ObjectUtils.isEmpty(merchantList)) {
                break;
            }

            offset += size;

            merchantList.forEach(this::handleQueryByMerchant);

        }

    }

    private void handleQueryByMerchant(Merchant merchant) {
        Long checkTime = System.currentTimeMillis() - 30 * 60 * 1000L;
        Long offset = 0L;
        Long startId = 0L;

        while (true) {
            List<MerchantWithdrawSendBO> merchantWithdrawSendBOList = merchantWithdrawApplicationService.listWithdrawingByMerchantId(merchant.getUid(), offset, startId, checkTime);
            if (ObjectUtils.isEmpty(merchantWithdrawSendBOList)) {
                break;
            }

            startId = merchantWithdrawSendBOList.get(merchantWithdrawSendBOList.size() - 1).getApplicationId();
            List<Long> idList = new ArrayList<>();

            merchantWithdrawSendBOList.forEach(merchantWithdrawSendBO -> {
                Long franchiseeId = MultiFranchiseeConstant.DEFAULT_FRANCHISEE;
                Integer tenantId = merchantWithdrawSendBO.getTenantId();

                // 批量提现审核的时候使用的支付配置为加盟商的配置
                if (Objects.equals(merchantWithdrawSendBO.getPayConfigType(), MerchantWithdrawApplicationRecordConstant.PAY_CONFIG_TYPE_FRANCHISEE)) {
                    franchiseeId = merchantWithdrawSendBO.getFranchiseeId();
                }

                //查询支付配置详情
                WechatPayParamsDetails details = null;

                try {
                    details = wechatPayParamsBizService.getDetailsByIdTenantIdAndFranchiseeId(tenantId, franchiseeId);
                } catch (Exception e) {
                    log.error("query merchant withdraw status error! get wechat pay params details error, tenantId = {}, franchiseeId={}", tenantId, franchiseeId, e);
                    return;
                }

                if (Objects.isNull(details)) {
                    log.error("query merchant withdraw status error! wechat pay params details is null, batchNo = {}, tenantId = {}, franchiseeId={}", tenantId, franchiseeId);
                    return;
                }

                String wechatMerchantId = merchantWithdrawSendBO.getWechatMerchantId();
                if (StringUtils.isNotEmpty(wechatMerchantId) && (!Objects.equals(wechatMerchantId, details.getWechatMerchantId()) || !Objects.equals(details.getFranchiseeId(),
                        franchiseeId))) {
                    idList.add(merchantWithdrawSendBO.getApplicationId());

                    log.warn("query merchant withdraw warn! wechat merchant id is not equal, batchDetailNo = {}, tenantId = {}, franchiseeId={}, oldWechatMerchantId = {}, newWechatMerchantId", merchantWithdrawSendBO.getBatchDetailNo(),
                            tenantId, franchiseeId, merchantWithdrawSendBO.getWechatMerchantId(), details.getWechatMerchantId());
                    return;
                }

                WechatTransferOrderRecordRequestV2 request = new WechatTransferOrderRecordRequestV2();
                request.setOutBillNo(merchantWithdrawSendBO.getBatchDetailNo());
                //转换支付接口调用参数
                WechatV3CommonRequest wechatV3CommonRequest = ElectricityPayParamsConverter.qryDetailsToCommonRequest(details);
                request.setCommonRequest(wechatV3CommonRequest);

                try {
                    WechatTransferOrderQueryResultV2 wechatTransferOrderQueryResultV2 = wechatV3TransferInvokeService.queryTransferOrderV2(request);

                    if (Objects.isNull(wechatTransferOrderQueryResultV2)) {
                        log.info("query merchant withdraw result v2 info, response is null, batchDetailNo = {}, response = {}",
                                merchantWithdrawSendBO.getBatchDetailNo(), wechatTransferOrderQueryResultV2);
                        return;
                    }

                    log.info("query wechat transfer order v2 result, result = {}, batchDetailNo = {}", wechatTransferOrderQueryResultV2,
                            merchantWithdrawSendBO.getBatchDetailNo());

                    // 判断回调是否正在处理
                    if (redisService.hasKey(String.format(CacheConstant.CACHE_MERCHANT_WITHDRAW_NOTIFY_LOCK, merchantWithdrawSendBO.getBatchDetailNo()))) {
                        log.info("query wechat transfer order v2 info, notify is processing, batchDetailNo = {}, response = {}",
                                merchantWithdrawSendBO.getBatchDetailNo(), wechatTransferOrderQueryResultV2);
                        return;
                    }

                    // 检测微信结果是否正常
                    if (Objects.isNull(MerchantWithdrawApplicationStateEnum.getStateByDesc(wechatTransferOrderQueryResultV2.getState()))) {
                        log.info("query wechat transfer order v2 info, state is error, batchDetailNo = {}, response = {}",
                                merchantWithdrawSendBO.getBatchDetailNo(), wechatTransferOrderQueryResultV2);
                        return;
                    }

                    // 检测微信的结果状态是否为终态
                    if (!(Objects.equals(wechatTransferOrderQueryResultV2.getState(), MerchantWithdrawApplicationStateEnum.SUCCESS.getDesc()) || Objects.equals(wechatTransferOrderQueryResultV2.getState(), MerchantWithdrawApplicationStateEnum.FAIL.getDesc())
                            || Objects.equals(wechatTransferOrderQueryResultV2.getState(), MerchantWithdrawApplicationStateEnum.CANCELLED.getDesc()))) {
                        log.info("query wechat transfer order v2 info, state is not success, batchDetailNo = {}, response = {}", merchantWithdrawSendBO.getBatchDetailNo(), wechatTransferOrderQueryResultV2);
                        return;
                    }

                    // 修改application的state
                    Integer state = MerchantWithdrawApplicationStateEnum.getStateByDesc(wechatTransferOrderQueryResultV2.getState()).getCode();
                    Integer count = merchantWithdrawApplicationService.updateStateById(merchantWithdrawSendBO.getApplicationId(), state);

                    // 检测是否修改成功
                    if (Objects.isNull(count) || Objects.equals(count, NumberConstant.ZERO)) {
                        return;
                    }

                    sendNotify(state, merchantWithdrawSendBO, tenantId);

                } catch (Exception e) {
                    log.error("query batch wechat transfer order v2 info error, e = {}, params = {}", e, request);
                }
            });

            if (ObjectUtils.isNotEmpty(idList)) {
                // 支付配置发送了改变
                merchantWithdrawApplicationService.batchUpdatePayConfigChangeByIdList(idList, MerchantWithdrawApplicationConstant.PAY_CONFIG_WHETHER_CHANGE_YES);
            }
        }
    }

    private void sendNotify(Integer state, MerchantWithdrawSendBO merchantWithdrawSendBO, Integer tenantId) throws AlipayApiException {
        // 检测是否符合发送通知的条件
        if ((Objects.equals(state, MerchantWithdrawApplicationStateEnum.WAIT_USER_CONFIRM.getCode()) || Objects.equals(state, MerchantWithdrawApplicationStateEnum.TRANSFERING.getCode()))
                && (Objects.equals(merchantWithdrawSendBO.getState(), MerchantWithdrawApplicationStateEnum.ACCEPTED.getCode())
                || Objects.equals(merchantWithdrawSendBO.getState(), MerchantWithdrawApplicationStateEnum.PROCESSING.getCode()))) {

            UserOauthBind userOauthBind = userOauthBindService.queryByUidAndTenantAndSource(merchantWithdrawSendBO.getUid(), tenantId, UserOauthBind.SOURCE_WX_PRO);
            if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
                log.warn("query wechat transfer order v2 warn, not found user auth bind info for merchant user. uid = {}", merchantWithdrawSendBO.getUid());
                return;
            }

            AppTemplateQuery appTemplateQuery = new AppTemplateQuery();
            appTemplateQuery.setAppId(merchantConfig.getMerchantAppletId());
            appTemplateQuery.setSecret(merchantConfig.getMerchantAppletSecret());
            appTemplateQuery.setTouser(userOauthBind.getThirdId());
            appTemplateQuery.setTemplateId(merchantConfig.getTemplateId());
            appTemplateQuery.setPage(merchantConfig.getPage());
            appTemplateQuery.setMiniProgramState(merchantConfig.getMiniProgramState());
            appTemplateQuery.setData(this.getData(merchantWithdrawSendBO));

            weChatAppTemplateService.sendMsg(appTemplateQuery);
        }
    }

    private Map<String, Object> getData(MerchantWithdrawSendBO merchantWithdrawSendBO) {
        Map<String, Object> data = new HashMap<>();
        data.put("amount1", merchantWithdrawSendBO.getAmount());
        data.put("phrase2", MerchantWithdrawConstant.PHRASE2);
        data.put("time3", DateUtil.format(new Date(merchantWithdrawSendBO.getCreateTime()), DateFormatConstant.MONTH_DATE_TIME_FORMAT));
        data.put("thing4", MerchantWithdrawConstant.RECEIVE_REMARK);
        return data;
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
