package com.xiliulou.electricity.handler.placeorder.chain;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupNamesBO;
import com.xiliulou.electricity.handler.placeorder.context.PlaceOrderContext;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.handler.placeorder.AbstractPlaceOrderHandler;
import com.xiliulou.electricity.query.userinfo.userInfoGroup.UserInfoGroupDetailQuery;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.PlaceOrderConstant.PLACE_ORDER_MEMBER_CARD;

/**
 * @Description 套餐校验处理节点，同时需要将后续处理需要的数据存入上下文对象context中
 * @Author: SongJP
 * @Date: 2024/10/25 17:12
 */
@Slf4j
@Component
public class MemberCardVerificationHandler extends AbstractPlaceOrderHandler {
    
    @Resource
    private MemberCardPlaceOrderHandler memberCardPlaceOrderHandler;
    
    @Resource
    private UserInfoGroupDetailService userInfoGroupDetailService;
    
    public MemberCardVerificationHandler() {
        this.nextHandler = memberCardPlaceOrderHandler;
        this.nodePlaceOrderType = PLACE_ORDER_MEMBER_CARD;
    }
    
    @Override
    public void dealWithBusiness(PlaceOrderContext context, R<Object> result, Integer placeOrderType) {
        UserInfo userInfo = context.getUserInfo();
        BatteryMemberCard batteryMemberCard = context.getBatteryMemberCard();
        
        // 查询用户分组供后续校验使用
        List<UserInfoGroupNamesBO> userInfoGroupNamesBos = userInfoGroupDetailService.listGroupByUid(
                UserInfoGroupDetailQuery.builder().uid(userInfo.getUid()).tenantId(TenantContextHolder.getTenantId()).build());
        
        // 判断套餐用户分组和用户的用户分组是否匹配
        if (CollectionUtils.isNotEmpty(userInfoGroupNamesBos)) {
            if (Objects.equals(batteryMemberCard.getGroupType(), BatteryMemberCard.GROUP_TYPE_SYSTEM)) {
                result = R.fail("100318", "您浏览的套餐已下架，请看看其他的吧");
                // 提前退出
                exit();
            }
            
            List<Long> userGroupIds = userInfoGroupNamesBos.stream().map(UserInfoGroupNamesBO::getGroupId).collect(Collectors.toList());
            userGroupIds.retainAll(JsonUtil.fromJsonArray(batteryMemberCard.getUserInfoGroupIds(), Long.class));
            if (CollectionUtils.isEmpty(userGroupIds)) {
                result = R.fail("100318", "您浏览的套餐已下架，请看看其他的吧");
                // 提前退出
                exit();
            }
        } else {
            if (Objects.equals(batteryMemberCard.getGroupType(), BatteryMemberCard.GROUP_TYPE_USER)) {
                result = R.fail("100318", "您浏览的套餐已下架，请看看其他的吧");
                // 提前退出
                exit();
            }
            
            // 判断套餐租赁状态，用户为老用户，套餐类型为新租，则不支持购买
            if (userInfo.getPayCount() > 0 && BatteryMemberCard.RENT_TYPE_NEW.equals(batteryMemberCard.getRentType())) {
                log.warn("INTEGRATED PAYMENT WARN! The rent type of current package is a new rental package, uid={}, mid={}", userInfo.getUid(), batteryMemberCard.getId());
                result = R.fail("100376", "已是平台老用户，无法购买新租类型套餐，请刷新页面重试");
            }
        }
        
        processEntryAndExit(context, result, placeOrderType);
    }
}
