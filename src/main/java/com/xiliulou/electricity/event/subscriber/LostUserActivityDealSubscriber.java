package com.xiliulou.electricity.event.subscriber;


import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.constant.merchant.MerchantInviterModifyRecordConstant;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.merchant.MerchantInviterModifyRecord;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.event.LostUserActivityDealEvent;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.lostuser.LostUserBizService;
import com.xiliulou.electricity.service.merchant.MerchantInviterModifyRecordService;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import com.xiliulou.electricity.ttl.TtlXllThreadPoolExecutorServiceWrapper;
import com.xiliulou.electricity.ttl.TtlXllThreadPoolExecutorsSupport;
import com.xiliulou.electricity.vo.merchant.MerchantInviterVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 流失用户套餐购买成功后活动处理
 *
 * @author maxiaodong
 * @description:
 * @date 2024/1/31 20:36
 */

@Slf4j
@Component
public class LostUserActivityDealSubscriber {
    private UserInfoExtraService userInfoExtraService;

    private LostUserBizService lostUserBizService;

    private MerchantInviterModifyRecordService merchantInviterModifyRecordService;

    private final TtlXllThreadPoolExecutorServiceWrapper serviceWrapper = TtlXllThreadPoolExecutorsSupport.get(
            XllThreadPoolExecutors.newFixedThreadPool("LOST-USER-ACTIVITY-DEAL-THREAD-POOL", 4, "lostUserActivityDealThread:"));

    public LostUserActivityDealSubscriber(UserInfoExtraService userInfoExtraService, LostUserBizService lostUserBizService, MerchantInviterModifyRecordService merchantInviterModifyRecordService) {
        this.userInfoExtraService = userInfoExtraService;
        this.lostUserBizService = lostUserBizService;
        this.merchantInviterModifyRecordService = merchantInviterModifyRecordService;
    }

    @EventListener
    public void handleLostUserActivityDealEvent(LostUserActivityDealEvent event) {
        if (Objects.isNull(event)) {
            log.warn("Received subscription event LostUserActivityDealEvent is null");
            return;
        }

        Long uid = event.getUid();
        Integer tenantId = event.getTenantId();

        log.info("Received subscription event LostUserActivityDealEvent:uid[{}] unfreezeUserActivityType[{}] orderId[{}]", uid, event.getUnfreezeUserActivityType(), event.getOrderId());

        serviceWrapper.execute(() -> {
            try {
                // 判断用户是否为流失用户
                UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(uid);
                if (Objects.isNull(userInfoExtra)) {
                    log.info("HANDLE LOST USER ACTIVITY DEAL EVENT INFO! userInfoExtra is null, uid：{}, orderId:{}", uid, event.getOrderId());
                    return;
                }

                if (!Objects.equals(userInfoExtra.getLostUserStatus(), YesNoEnum.YES.getCode())) {
                    log.info("HANDLE LOST USER ACTIVITY DEAL EVENT INFO! user is not lost user, uid：{}, orderId:{}", uid, event.getOrderId());
                    return;
                }

                MerchantInviterVO successInviterVO = userInfoExtraService.querySuccessInviter(uid);
                if (Objects.isNull(successInviterVO)) {
                    log.info("HANDLE LOST USER ACTIVITY DEAL EVENT INFO! inviter is empty, uid：{}, orderId:{}", uid, event.getOrderId());
                }

                Long oldInviterUid = null;
                String oldInviterName = null;
                Integer inviterSource = null;
                if (Objects.nonNull(successInviterVO)) {
                    oldInviterUid = successInviterVO.getInviterUid();
                    oldInviterName = successInviterVO.getInviterName();
                    inviterSource = successInviterVO.getInviterSource();
                }

                // 流失用户修改为老用户且解绑活动
                lostUserBizService.updateLostUserStatusAndUnbindActivity(tenantId, uid, successInviterVO);

                // 如果用户之前绑定过活动则修改流失用户为无活动状态
                if (Objects.nonNull(successInviterVO)) {
                    // 修改用户为无参与活动记录
                    if (Objects.equals(event.getUnfreezeUserActivityType(), YesNoEnum.YES.getCode())) {
                        lostUserBizService.updateLostUserNotActivity(uid);
                    }

                    // 新租修改邀请记录
                    MerchantInviterModifyRecord merchantInviterModifyRecord = MerchantInviterModifyRecord.builder().uid(uid).inviterUid(NumberConstant.ZERO_L)
                            .inviterName("").oldInviterUid(oldInviterUid)
                            .oldInviterName(oldInviterName).oldInviterSource(inviterSource).merchantId(NumberConstant.ZERO_L).franchiseeId(NumberConstant.ZERO_L).tenantId(tenantId)
                            .operator(NumberConstant.ZERO_L).remark(MerchantInviterModifyRecordConstant.LOST_USER_MODIFY_INVITER_CANCEL_REMARK).delFlag(MerchantConstant.DEL_NORMAL).createTime(System.currentTimeMillis())
                            .updateTime(System.currentTimeMillis()).build();

                    merchantInviterModifyRecordService.insertOne(merchantInviterModifyRecord);
                }
            } catch (Exception e) {
                log.error("LOST USER ACTIVITY DEAL SUBSCRIBER ERROR", e);
            }
        });
    }
}
