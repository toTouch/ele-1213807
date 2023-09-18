package com.xiliulou.electricity.controller.admin;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.WithdrawRecord;
import com.xiliulou.electricity.query.HandleWithdrawQuery;
import com.xiliulou.electricity.query.WithdrawRecordQuery;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.WithdrawRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author: Miss.Li
 * @Date: 2021/8/30 09:33
 * @Description:
 */
@RestController
@Slf4j
public class JsonAdminWithdrawController extends BaseController {
    @Autowired
    WithdrawRecordService withdrawRecordService;

    @Autowired
    RedisService redisService;

    @Autowired
    UserService userService;

    @Autowired
    WechatConfig wechatConfig;

    @Autowired
    UserDataScopeService userDataScopeService;

    @PostMapping(value = "/admin/handleWithdraw")
	@Log(title = "提现审核")
	public R withdraw(@Validated @RequestBody HandleWithdrawQuery handleWithdrawQuery) {
		/*Integer tenantId = TenantContextHolder.getTenantId();
		if(!Objects.equals(tenantId,wechatConfig.getTenantId())){
			return R.fail("ELECTRICITY.0066", "用户权限不足");
		}*/
        return withdrawRecordService.handleWithdraw(handleWithdrawQuery);
    }

    @GetMapping(value = "/admin/withdraw/list")
    public R queryList(@RequestParam(value = "size", required = false) Long size,
                       @RequestParam(value = "offset", required = false) Long offset,
                       @RequestParam(value = "uid", required = false) Long uid,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime,
                       @RequestParam(value = "status", required = false) Integer status,
                       @RequestParam(value = "orderId", required = false) String orderId,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "type", required = false) Integer type) {
        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Integer> statusList = new ArrayList<>();
        if (Objects.equals(status, -1)) {
            statusList.add(WithdrawRecord.CHECK_PASS);
            statusList.add(WithdrawRecord.WITHDRAWING);
        } else {
            if (Objects.nonNull(status)) {
                statusList.add(status);
            }
        }

        WithdrawRecordQuery withdrawRecordQuery = WithdrawRecordQuery.builder()
                .offset(offset)
                .size(size)
                .uid(uid)
                .beginTime(beginTime)
                .endTime(endTime)
                .status(statusList)
                .orderId(orderId)
                .phone(phone)
                .type(type)
                .storeIds(storeIds)
                .franchiseeIds(franchiseeIds)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return withdrawRecordService.queryList(withdrawRecordQuery);
    }

    @GetMapping(value = "/admin/withdraw/queryCount")
    public R queryCount(@RequestParam(value = "uid", required = false) Long uid,
                        @RequestParam(value = "beginTime", required = false) Long beginTime,
                        @RequestParam(value = "endTime", required = false) Long endTime,
                        @RequestParam(value = "status", required = false) Integer status,
                        @RequestParam(value = "orderId", required = false) String orderId,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "type", required = false) Integer type) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Integer> statusList = new ArrayList<>();
        if (Objects.equals(status, -1)) {
            statusList.add(WithdrawRecord.CHECK_PASS);
            statusList.add(WithdrawRecord.WITHDRAWING);
        } else {
            if (Objects.nonNull(status)) {
                statusList.add(status);
            }
        }

        WithdrawRecordQuery withdrawRecordQuery = WithdrawRecordQuery.builder()
                .uid(uid)
                .beginTime(beginTime)
                .endTime(endTime)
                .status(statusList)
                .orderId(orderId)
                .phone(phone)
                .type(type)
                .storeIds(storeIds)
                .franchiseeIds(franchiseeIds)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return withdrawRecordService.queryCount(withdrawRecordQuery);
    }

}
