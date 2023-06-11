package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.ChannelActivity;
import com.xiliulou.electricity.entity.ShareActivity;
import com.xiliulou.electricity.entity.ShareMoneyActivity;
import com.xiliulou.electricity.entity.ShareMoneyActivityRecord;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.ShareActivityMapper;
import com.xiliulou.electricity.mapper.ShareMoneyActivityMapper;
import com.xiliulou.electricity.query.ShareMoneyActivityAddAndUpdateQuery;
import com.xiliulou.electricity.query.ShareMoneyActivityQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ShareMoneyActivityVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 活动表(TActivity)表服务实现类
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
@Service("shareMoneyActivityService")
@Slf4j
public class ShareMoneyActivityServiceImpl implements ShareMoneyActivityService {
    @Resource
    private ShareMoneyActivityMapper shareMoneyActivityMapper;

    @Autowired
    RedisService redisService;

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    ShareMoneyActivityRecordService shareMoneyActivityRecordService;

    @Resource
    ShareActivityMapper shareActivityMapper;
    
    @Autowired
    ChannelActivityService channelActivityService;

    @Autowired
    InvitationActivityService invitationActivityService;


    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ShareMoneyActivity queryByIdFromCache(Integer id) {
        //先查缓存
        ShareMoneyActivity shareMoneyActivityCache = redisService.getWithHash(CacheConstant.SHARE_MONEY_ACTIVITY_CACHE + id, ShareMoneyActivity.class);
        if (Objects.nonNull(shareMoneyActivityCache)) {
            return shareMoneyActivityCache;
        }

        //缓存没有再查数据库
        ShareMoneyActivity shareMoneyActivity = shareMoneyActivityMapper.selectById(id);
        if (Objects.isNull(shareMoneyActivity)) {
            return null;
        }

        //放入缓存
        redisService.saveWithHash(CacheConstant.SHARE_MONEY_ACTIVITY_CACHE + id, shareMoneyActivity);
        return shareMoneyActivity;
    }

    /**
     * 新增数据
     *
     * @param shareMoneyActivityAddAndUpdateQuery 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R insert(ShareMoneyActivityAddAndUpdateQuery shareMoneyActivityAddAndUpdateQuery) {
        //创建账号
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("Coupon  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //查询该租户是否有邀请活动，有则不能添加
        int count = shareMoneyActivityMapper.selectCount(new LambdaQueryWrapper<ShareMoneyActivity>()
                .eq(ShareMoneyActivity::getTenantId, tenantId).eq(ShareMoneyActivity::getStatus, ShareMoneyActivity.STATUS_ON));
        if (count > 0) {
            return R.fail("ELECTRICITY.00102", "该租户已有启用中的邀请活动，请勿重复添加");
        }


        ShareMoneyActivity shareMoneyActivity = new ShareMoneyActivity();
        BeanUtil.copyProperties(shareMoneyActivityAddAndUpdateQuery, shareMoneyActivity);
        shareMoneyActivity.setUid(user.getUid());
        shareMoneyActivity.setUserName(user.getUsername());
        shareMoneyActivity.setCreateTime(System.currentTimeMillis());
        shareMoneyActivity.setUpdateTime(System.currentTimeMillis());
        shareMoneyActivity.setTenantId(tenantId);

        if (Objects.isNull(shareMoneyActivity.getType())) {
            shareMoneyActivity.setType(ShareActivity.SYSTEM);
        }

        int insert = shareMoneyActivityMapper.insert(shareMoneyActivity);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            //放入缓存
            redisService.saveWithHash(CacheConstant.SHARE_MONEY_ACTIVITY_CACHE + shareMoneyActivity.getId(), shareMoneyActivity);
            return null;
        });

        if (insert > 0) {
            return R.ok(shareMoneyActivity.getId());
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }

    /**
     * 修改数据(暂只支持上下架）
     *
     * @param shareMoneyActivityAddAndUpdateQuery 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R update(ShareMoneyActivityAddAndUpdateQuery shareMoneyActivityAddAndUpdateQuery) {
        ShareMoneyActivity oldShareMoneyActivity = queryByIdFromCache(shareMoneyActivityAddAndUpdateQuery.getId());
        if (Objects.isNull(oldShareMoneyActivity)) {
            log.error("update Activity  ERROR! not found Activity ! ActivityId:{} ", shareMoneyActivityAddAndUpdateQuery.getId());
            return R.fail("ELECTRICITY.0069", "未找到活动");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //查询该租户是否有邀请活动，有则不能启用
        if (Objects.equals(shareMoneyActivityAddAndUpdateQuery.getStatus(), ShareMoneyActivity.STATUS_ON)) {
            int count = shareMoneyActivityMapper.selectCount(new LambdaQueryWrapper<ShareMoneyActivity>()
                    .eq(ShareMoneyActivity::getTenantId, tenantId).eq(ShareMoneyActivity::getStatus, ShareMoneyActivity.STATUS_ON));
            if (count > 0) {
                return R.fail("ELECTRICITY.00102", "该租户已有启用中的邀请活动，请勿重复添加");
            }
        }

        ShareMoneyActivity shareMoneyActivity = new ShareMoneyActivity();
        BeanUtil.copyProperties(shareMoneyActivityAddAndUpdateQuery, shareMoneyActivity);
        shareMoneyActivity.setUpdateTime(System.currentTimeMillis());

        int update = shareMoneyActivityMapper.updateById(shareMoneyActivity);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.delete(CacheConstant.SHARE_ACTIVITY_CACHE + oldShareMoneyActivity.getId());
            return null;
        });

        if (update > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }

    @Override
    public R queryList(ShareMoneyActivityQuery shareMoneyActivityQuery) {
        List<ShareMoneyActivity> shareMoneyActivityList = shareMoneyActivityMapper.queryList(shareMoneyActivityQuery);
        return R.ok(shareMoneyActivityList);
    }


    @Override
    public R queryCount(ShareMoneyActivityQuery shareMoneyActivityQuery) {
        Integer count = shareMoneyActivityMapper.queryCount(shareMoneyActivityQuery);
        return R.ok(count);
    }


    @Override
    public R queryInfo(Integer id) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        ShareMoneyActivity shareMoneyActivity = queryByIdFromCache(id);
        if (Objects.isNull(shareMoneyActivity)) {
            log.error("queryInfo Activity  ERROR! not found Activity ! ActivityId:{} ", id);
            return R.fail("ELECTRICITY.0069", "未找到活动");
        }

        if (!Objects.equals(tenantId, shareMoneyActivity.getTenantId())) {
            return R.ok();
        }

        return R.ok(shareMoneyActivity);
    }


    @Override
    public ShareMoneyActivity queryByStatus(Integer activityId) {
        return shareMoneyActivityMapper.selectOne(new LambdaQueryWrapper<ShareMoneyActivity>()
                .eq(ShareMoneyActivity::getId, activityId).eq(ShareMoneyActivity::getStatus, ShareMoneyActivity.STATUS_ON));
    }

    @Override
    public R activityInfo() {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("order  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //用户是否可用
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELECTRICITY  ERROR! not found userInfo,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("order  ERROR! user not auth,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //邀请活动
        ShareMoneyActivity shareMoneyActivity = shareMoneyActivityMapper.selectOne(new LambdaQueryWrapper<ShareMoneyActivity>()
                .eq(ShareMoneyActivity::getTenantId, tenantId).eq(ShareMoneyActivity::getStatus, ShareMoneyActivity.STATUS_ON));
        if (Objects.isNull(shareMoneyActivity)) {
            log.error("queryInfo Activity  ERROR! not found Activity ! tenantId:{} ", tenantId);
            return R.ok();
        }


        ShareMoneyActivityVO shareMoneyActivityVO = new ShareMoneyActivityVO();
        BeanUtil.copyProperties(shareMoneyActivity, shareMoneyActivityVO);

        //邀请好友数
        int count = 0;
        BigDecimal totalMoney = BigDecimal.ZERO;
        ShareMoneyActivityRecord shareMoneyActivityRecord = shareMoneyActivityRecordService.queryByUid(user.getUid(), shareMoneyActivityVO.getId());
        if (Objects.nonNull(shareMoneyActivityRecord)) {
            count = shareMoneyActivityRecord.getCount();
            totalMoney = shareMoneyActivityRecord.getMoney();
        }

        shareMoneyActivityVO.setCount(count);
        shareMoneyActivityVO.setTotalMoney(totalMoney);

        return R.ok(shareMoneyActivityVO);
    }

    @Override
    public R checkActivity() {

        Map<String, Integer> map = new HashMap<>();
        map.put("shareMoneyActivity", 0);
        map.put("shareActivity", 0);
        map.put("channelActivity", 0);
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("order  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //用户是否可用
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELECTRICITY  ERROR! not found userInfo,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("order  ERROR! user not auth,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }


        //邀请返现活动
        ShareMoneyActivity shareMoneyActivity = shareMoneyActivityMapper.selectOne(new LambdaQueryWrapper<ShareMoneyActivity>()
                .eq(ShareMoneyActivity::getTenantId, tenantId).eq(ShareMoneyActivity::getStatus, ShareMoneyActivity.STATUS_ON));
        if (Objects.isNull(shareMoneyActivity)) {
//            log.error("queryInfo Activity  ERROR! not found Activity ! tenantId:{} ", tenantId);
            map.put("shareMoneyActivity", 1);
        }

        //邀请活动
        ShareActivity shareActivity = shareActivityMapper.selectOne(new LambdaQueryWrapper<ShareActivity>()
                .eq(ShareActivity::getTenantId, tenantId).eq(ShareActivity::getStatus, ShareActivity.STATUS_ON));
        if (Objects.isNull(shareActivity)) {
//            log.error("queryInfo Activity  ERROR! not found Activity ! tenantId:{} ", tenantId);
            map.put("shareActivity", 1);
        }
    
        //渠道活动
        ChannelActivity usableActivity = channelActivityService.findUsableActivity(tenantId);
        if (Objects.isNull(usableActivity)) {
            map.put("channelActivity", 1);
        }

        Integer invitationActivity = invitationActivityService.checkUsableActivity(tenantId);
        if(Objects.isNull(invitationActivity)){
            map.put("invitationActivity", 1);
        }else{
            map.put("invitationActivity", 0);
        }

        return R.ok(map);
    }

}

