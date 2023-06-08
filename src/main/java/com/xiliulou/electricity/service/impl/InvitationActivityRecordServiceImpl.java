package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.InvitationActivityRecordMapper;
import com.xiliulou.electricity.query.InvitationActivityQuery;
import com.xiliulou.electricity.query.InvitationActivityRecordQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.AESUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.InvitationActivityCodeVO;
import com.xiliulou.electricity.vo.InvitationActivityRecordInfoVO;
import com.xiliulou.electricity.vo.InvitationActivityRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * (InvitationActivityRecord)表服务实现类
 *
 * @author zzlong
 * @since 2023-06-05 20:17:53
 */
@Service("invitationActivityRecordService")
@Slf4j
public class InvitationActivityRecordServiceImpl implements InvitationActivityRecordService {
    @Resource
    private InvitationActivityRecordMapper invitationActivityRecordMapper;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private InvitationActivityUserService invitationActivityUserService;

    @Autowired
    private InvitationActivityService invitationActivityService;

    @Autowired
    private UserBatteryMemberCardService userBatteryMemberCardService;

    @Autowired
    private InvitationActivityJoinHistoryService invitationActivityJoinHistoryService;

    @Autowired
    private InvitationActivityMemberCardService invitationActivityMemberCardService;

    @Autowired
    private UserAmountService userAmountService;

    @Autowired
    private ElectricityMemberCardOrderService electricityMemberCardOrderService;

    @Override
    public List<InvitationActivityRecordVO> selectByPage(InvitationActivityRecordQuery query) {
        List<InvitationActivityRecordVO> list = invitationActivityRecordMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        return list.parallelStream().peek(item -> {

            InvitationActivity invitationActivity = invitationActivityService.queryByIdFromCache(item.getActivityId());
            item.setActivityName(Objects.isNull(invitationActivity) ? "" : invitationActivity.getName());

        }).collect(Collectors.toList());
    }

    @Override
    public Integer selectByPageCount(InvitationActivityRecordQuery query) {
        return invitationActivityRecordMapper.selectByPageCount(query);
    }

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public InvitationActivityRecord queryByIdFromDB(Long id) {
        return this.invitationActivityRecordMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public InvitationActivityRecord queryByIdFromCache(Long id) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<InvitationActivityRecord> queryAllByLimit(int offset, int limit) {
        return this.invitationActivityRecordMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param invitationActivityRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvitationActivityRecord insert(InvitationActivityRecord invitationActivityRecord) {
        this.invitationActivityRecordMapper.insertOne(invitationActivityRecord);
        return invitationActivityRecord;
    }

    /**
     * 修改数据
     *
     * @param invitationActivityRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(InvitationActivityRecord invitationActivityRecord) {
        return this.invitationActivityRecordMapper.update(invitationActivityRecord);

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.invitationActivityRecordMapper.deleteById(id) > 0;
    }

    @Override
    public Integer addCountAndMoneyByUid(BigDecimal rewardAmount, Long recordId) {
        return this.invitationActivityRecordMapper.addCountAndMoneyByUid(rewardAmount, recordId);
    }

    @Override
    public Integer addMoneyByRecordId(BigDecimal rewardAmount, Long recordId) {
        return this.invitationActivityRecordMapper.addMoneyByRecordId(rewardAmount, recordId);
    }

    @Override
    public InvitationActivityRecord selectByActivityIdAndUid(Long activityId, Long uid) {
        return this.invitationActivityRecordMapper.selectByActivityIdAndUid(activityId, uid);
    }

    @Override
    public Triple<Boolean, String, Object> selectUserInvitationDetail() {

        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("INVITATION ACTIVITY ERROR! not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("INVITATION ACTIVITY ERROR! user is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("INVITATION ACTIVITY ERROR! user not auth,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }

        //获取当前上架的活动
        InvitationActivity invitationActivity = invitationActivityService.selectUsableActivity(TenantContextHolder.getTenantId());
        if (Objects.isNull(invitationActivity)) {
            log.error("INVITATION ACTIVITY ERROR! not found InvitationActivity,uid={}", userInfo.getUid());
            return Triple.of(false, "100391", "暂无上架的活动");
        }

        InvitationActivityRecord activityRecord = this.selectByActivityIdAndUid(invitationActivity.getId(), userInfo.getUid());
        if (Objects.isNull(activityRecord)) {
            return Triple.of(true, null, null);
        }

        InvitationActivityRecordInfoVO invitationActivityRecordInfoVO = new InvitationActivityRecordInfoVO();
        BeanUtils.copyProperties(activityRecord, invitationActivityRecordInfoVO);

        return Triple.of(true, null, invitationActivityRecordInfoVO);
    }

    @Override
    public Triple<Boolean, String, Object> generateCode() {
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            return Triple.of(false, "100001", "用户不存在");
        }

        InvitationActivity invitationActivity = invitationActivityService.selectUsableActivity(TenantContextHolder.getTenantId());
        if (Objects.isNull(invitationActivity)) {
            log.error("INVITATION ACTIVITY ERROR! not found InvitationActivity,uid={}", userInfo.getUid());
            return Triple.of(false, "100391", "暂无上架的活动");
        }

        InvitationActivityUser invitationActivityUser = invitationActivityUserService.selectByUid(userInfo.getUid());
        if (Objects.isNull(invitationActivityUser)) {
            log.error("INVITATION ACTIVITY ERROR! invitationActivityUser is null,uid={}", userInfo.getUid());
            return Triple.of(false, "100392", "无权限参加此活动");
        }

        if (StringUtils.isBlank(userInfo.getPhone())) {
            log.error("INVITATION ACTIVITY ERROR! phone is null,uid={}", userInfo.getUid());
            return Triple.of(false, "000001", "系统异常");
        }

        Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(tenant) || StringUtils.isBlank(tenant.getCode())) {
            log.error("INVITATION ACTIVITY ERROR! tenant is null,uid={}", userInfo.getUid());
            return Triple.of(false, "000001", "系统异常");
        }

        InvitationActivityCodeVO invitationActivityCodeVO = new InvitationActivityCodeVO();
        invitationActivityCodeVO.setCode(codeEnCoder(invitationActivity.getId(), userInfo.getUid()));
        invitationActivityCodeVO.setTenantCode(tenant.getCode());
        invitationActivityCodeVO.setPhone(userInfo.getPhone());

        InvitationActivityRecord invitationActivityRecord = invitationActivityRecordMapper.selectOne(new LambdaQueryWrapper<InvitationActivityRecord>()
                .eq(InvitationActivityRecord::getUid, userInfo.getUid()).eq(InvitationActivityRecord::getActivityId, invitationActivity.getId()));
        if (Objects.isNull(invitationActivityRecord)) {
            //第一次分享  生成分享记录
            InvitationActivityRecord invitationActivityRecordInsert = new InvitationActivityRecord();
            invitationActivityRecordInsert.setActivityId(invitationActivity.getId());
            invitationActivityRecordInsert.setUid(userInfo.getUid());
            invitationActivityRecordInsert.setCode(RandomUtil.randomNumbers(6));
            invitationActivityRecordInsert.setShareCount(0);
            invitationActivityRecordInsert.setInvitationCount(0);
            invitationActivityRecordInsert.setMoney(BigDecimal.ZERO);
            invitationActivityRecordInsert.setTenantId(TenantContextHolder.getTenantId());
            invitationActivityRecordInsert.setStatus(InvitationActivityRecord.STATUS_SUCCESS);
            invitationActivityRecordInsert.setCreateTime(System.currentTimeMillis());
            invitationActivityRecordInsert.setUpdateTime(System.currentTimeMillis());

            invitationActivityRecordMapper.insertOne(invitationActivityRecordInsert);
        }

        return Triple.of(true, null, invitationActivityCodeVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> joinActivity(InvitationActivityQuery query) {
        if (!redisService.setNx(CacheConstant.CACHE_SCAN_INTO_ACTIVITY_LOCK + SecurityUtils.getUid(), "1", 2000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("INVITATION ACTIVITY ERROR! not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        //是否已购买套餐
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userBatteryMemberCard) && Objects.nonNull(userBatteryMemberCard.getCardPayCount()) && userBatteryMemberCard.getCardPayCount() > 0) {
            return Triple.of(true, null, null);
        }

        String decrypt = null;
        try {
            decrypt = codeDeCoder(query.getCode());
        } catch (Exception e) {
            log.error("INVITATION ACTIVITY ERROR! decode fail,uid={},code={}", SecurityUtils.getUid(), query.getCode());
        }

        if (StringUtils.isBlank(decrypt)) {
            log.error("INVITATION ACTIVITY ERROR! invitation activity code decrypt error,code={}, uid={}", query.getCode(), userInfo.getUid());
            return Triple.of(false, "100457", "活动二维码解析失败");
        }

        String[] split = decrypt.split(":");
        if (split == null || split.length != 2) {
            log.error("INVITATION ACTIVITY ERROR! illegal code! code={}, uid={}", query.getCode(), userInfo.getUid());
            return Triple.of(false, "100459", "活动二维码内容不合法");
        }

        Long activityId = Long.parseLong(split[0]);
        Long invitationUid = Long.parseLong(split[1]);
        if (Objects.equals(userInfo.getUid(), invitationUid)) {
            log.info("INVITATION ACTIVITY INFO! illegal operate! invitationUid={}, uid={}", invitationUid, userInfo.getUid());
            return Triple.of(true, null, null);
        }

        InvitationActivity invitationActivity = invitationActivityService.queryByIdFromCache(activityId);
        if (Objects.isNull(invitationActivity) || !Objects.equals(invitationActivity.getStatus(), InvitationActivity.STATUS_UP)) {
            log.error("INVITATION ACTIVITY ERROR! invitationActivity disable,activityId={}, uid={}", activityId, userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.00106", "活动已下架");
        }

        UserInfo invitationUserInfo = userInfoService.queryByUidFromCache(invitationUid);
        if (Objects.isNull(invitationUserInfo)) {
            log.error("INVITATION ACTIVITY ERROR! not found invitationUserInfo,invitationUid={},uid={}", invitationUid, userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        //用户是否已参与过此活动
//        InvitationActivityJoinHistory invitationActivityJoinHistory = invitationActivityJoinHistoryService.selectByActivityAndInvitationUid(invitationActivity.getId(), invitationUid, userInfo.getUid());
        InvitationActivityJoinHistory invitationActivityJoinHistory = invitationActivityJoinHistoryService.selectByActivityAndUid(invitationActivity.getId(), userInfo.getUid());
        if (Objects.nonNull(invitationActivityJoinHistory)) {
            log.error("INVITATION ACTIVITY ERROR! user already join invitation activity,activityId={},uid={}", invitationActivity.getId(), userInfo.getUid());
            return Triple.of(true, null, null);
        }

        //获取活动记录
        InvitationActivityRecord invitationActivityRecord = invitationActivityRecordMapper.selectOne(new LambdaQueryWrapper<InvitationActivityRecord>()
                .eq(InvitationActivityRecord::getUid, invitationUid).eq(InvitationActivityRecord::getActivityId, invitationActivity.getId()));
        if (Objects.isNull(invitationActivityRecord)) {
            log.error("INVITATION ACTIVITY ERROR! invitationActivityRecord is null,activityId={}, invitationUid={}, uid={}", activityId, invitationUid, userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.00106", "活动已下架");
        }

        //更新活动邀请总人数
        invitationActivityRecordMapper.addShareCount(invitationActivityRecord.getId());

        //保存活动参与记录
        InvitationActivityJoinHistory invitationActivityJoinHistoryInsert = new InvitationActivityJoinHistory();
        invitationActivityJoinHistoryInsert.setUid(invitationUid);
        invitationActivityJoinHistoryInsert.setJoinUid(userInfo.getUid());
        invitationActivityJoinHistoryInsert.setActivityId(activityId);
        invitationActivityJoinHistoryInsert.setRecordId(invitationActivityRecord.getId());
        invitationActivityJoinHistoryInsert.setStatus(InvitationActivityJoinHistory.STATUS_INIT);
        invitationActivityJoinHistoryInsert.setStartTime(System.currentTimeMillis());
        invitationActivityJoinHistoryInsert.setExpiredTime(System.currentTimeMillis() + invitationActivity.getHours() * 60 * 60 * 1000L);
        invitationActivityJoinHistoryInsert.setTenantId(TenantContextHolder.getTenantId());
        invitationActivityJoinHistoryInsert.setCreateTime(System.currentTimeMillis());
        invitationActivityJoinHistoryInsert.setUpdateTime(System.currentTimeMillis());

        invitationActivityJoinHistoryService.insert(invitationActivityJoinHistoryInsert);

        return Triple.of(true, null, null);
    }

    @Override
    public void handleInvitationActivity(UserInfo userInfo, String orderId) {
        try {
            //是否有上架的套餐返现活动
            InvitationActivity invitationActivity = invitationActivityService.selectUsableActivity(userInfo.getTenantId());
            if (Objects.isNull(invitationActivity)) {
                return;
            }

            //是否参与过套餐返现活动
            InvitationActivityJoinHistory activityJoinHistory = invitationActivityJoinHistoryService.selectByActivityAndUid(invitationActivity.getId(), userInfo.getUid());
            if (Objects.isNull(activityJoinHistory)) {
                return;
            }

            ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(orderId);
            if (Objects.isNull(electricityMemberCardOrder)) {
                log.info("INVITATION ACTIVITY INFO!not found electricityMemberCardOrder,orderId={},uid={}", orderId, userInfo.getUid());
                return;
            }

            //是否购买的是活动指定的套餐
            List<Long> memberCardIds = invitationActivityMemberCardService.selectMemberCardIdsByActivityId(activityJoinHistory.getActivityId());
            if (CollectionUtils.isEmpty(memberCardIds) || !memberCardIds.contains(electricityMemberCardOrder.getMemberCardId().longValue())) {
                log.info("INVITATION ACTIVITY INFO!invite fail,activityId={},membercardId={},uid={}", activityJoinHistory.getActivityId(), electricityMemberCardOrder.getMemberCardId(), userInfo.getUid());
                return;
            }

            //返现金额
            BigDecimal rewardAmount = null;

            //首次购买套餐
            if (electricityMemberCardOrder.getPayCount() == 1) {
                //首次购买需要判断活动是否过期
                if (activityJoinHistory.getExpiredTime() < System.currentTimeMillis()) {
                    log.error("INVITATION ACTIVITY INFO!activity already sold out,activityId={},uid={}", activityJoinHistory.getActivityId(), userInfo.getUid());
                    return;
                }

                rewardAmount = invitationActivity.getFirstReward();
                //修改参与状态
                InvitationActivityJoinHistory activityJoinHistoryUpdate = new InvitationActivityJoinHistory();
                activityJoinHistoryUpdate.setId(activityJoinHistory.getId());
                activityJoinHistoryUpdate.setStatus(InvitationActivityJoinHistory.STATUS_SUCCESS);
                activityJoinHistoryUpdate.setMoney(rewardAmount);
                activityJoinHistoryUpdate.setPayCount(electricityMemberCardOrder.getPayCount());
                activityJoinHistoryUpdate.setUpdateTime(System.currentTimeMillis());
                invitationActivityJoinHistoryService.update(activityJoinHistoryUpdate);

                //给邀请人增加邀请成功人数及返现金额
                this.addCountAndMoneyByUid(rewardAmount, activityJoinHistory.getRecordId());
            } else {
                //非首次购买需要判断 首次购买是否成功
                if (!Objects.equals(activityJoinHistory.getStatus(), InvitationActivityJoinHistory.STATUS_SUCCESS)) {
                    log.error("INVITATION ACTIVITY INFO!activity join fail,activityHistoryId={},uid={}", activityJoinHistory.getId(), userInfo.getUid());
                    return;
                }

                rewardAmount = invitationActivity.getOtherReward();
                //保存参与记录
                InvitationActivityJoinHistory activityJoinHistoryInsert = new InvitationActivityJoinHistory();
                activityJoinHistoryInsert.setUid(activityJoinHistory.getUid());
                activityJoinHistoryInsert.setRecordId(activityJoinHistory.getRecordId());
                activityJoinHistoryInsert.setJoinUid(activityJoinHistory.getJoinUid());
                activityJoinHistoryInsert.setStartTime(activityJoinHistory.getStartTime());
                activityJoinHistoryInsert.setExpiredTime(activityJoinHistory.getExpiredTime());
                activityJoinHistoryInsert.setActivityId(activityJoinHistory.getActivityId());
                activityJoinHistoryInsert.setStatus(activityJoinHistory.getStatus());
                activityJoinHistoryInsert.setPayCount(electricityMemberCardOrder.getPayCount());
                activityJoinHistoryInsert.setMoney(rewardAmount);
                activityJoinHistoryInsert.setTenantId(userInfo.getTenantId());
                activityJoinHistoryInsert.setCreateTime(System.currentTimeMillis());
                activityJoinHistoryInsert.setUpdateTime(System.currentTimeMillis());
                invitationActivityJoinHistoryService.insert(activityJoinHistoryInsert);

                //给邀请人增加返现金额
                this.addMoneyByRecordId(rewardAmount, activityJoinHistory.getRecordId());
            }


            //处理返现
            userAmountService.handleInvitationActivityAmount(userInfo, activityJoinHistory.getUid(), rewardAmount);
        } catch (Exception e) {
            log.error("ELE ERROR!handle invitation activity fail,uid={},orderId={}", userInfo.getUid(), orderId, e);
        }
    }

    private static String codeEnCoder(Long activityId, Long uid) {
        String encrypt = AESUtils.encrypt(activityId + ":" + uid);

        if (StringUtils.isNotBlank(encrypt)) {
            Base64.Encoder encoder = Base64.getUrlEncoder();
            byte[] base64Result = encoder.encode(encrypt.getBytes());
            return new String(base64Result);
        }
        return null;
    }

    private static String codeDeCoder(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }

        Base64.Decoder decoder = Base64.getUrlDecoder();
        byte[] decode = decoder.decode(code.getBytes());
        String base64Result = new String(decode);

        if (StringUtils.isNotBlank(base64Result)) {
            return AESUtils.decrypt(base64Result);
        }
        return null;
    }


    public static void main(String[] args) {
        String a = codeEnCoder(6L, 1150216L);
        System.out.println(a);
        String b = codeDeCoder("d1dNWGhCM1pDcDFRR3ZyK05mT0R1dz09");
        System.out.println(b);
    }
}
