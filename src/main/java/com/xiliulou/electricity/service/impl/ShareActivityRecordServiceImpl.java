package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.MultiFranchiseeConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ShareActivityRecordMapper;
import com.xiliulou.electricity.query.ShareActivityRecordQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.asset.AssertPermissionService;
import com.xiliulou.electricity.service.pay.PayConfigBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.QrCodeParamVO;
import com.xiliulou.electricity.vo.ShareActivityRecordExcelVO;
import com.xiliulou.electricity.vo.ShareActivityRecordVO;
import com.xiliulou.pay.weixin.entity.SharePicture;
import com.xiliulou.pay.weixin.shareUrl.GenerateShareUrlService;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 发起邀请活动记录(ShareActivityRecord)表服务实现类
 *
 * @author makejava
 * @since 2021-07-14 09:45:04
 */
@Service("shareActivityRecordService")
@Slf4j
public class ShareActivityRecordServiceImpl implements ShareActivityRecordService {

    @Resource
    private ShareActivityRecordMapper shareActivityRecordMapper;

    @Autowired
    RedisService redisService;

    @Autowired
    GenerateShareUrlService generateShareUrlService;

    @Autowired
    ElectricityPayParamsService electricityPayParamsService;

    @Resource
    private PayConfigBizService payConfigBizService;

    @Autowired
    ShareActivityService shareActivityService;

    @Autowired
    UserService userService;

    @Autowired
    private ShareActivityRuleService shareActivityRuleService;

    @Autowired
    private CouponService couponService;

    @Autowired
    UserCouponService userCouponService;

    @Autowired
    private AssertPermissionService assertPermissionService;

    @Resource
    private TenantService tenantService;


    @Resource
    private FranchiseeService franchiseeService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    @Slave
    public ShareActivityRecord queryByIdFromDB(Long id) {
        return this.shareActivityRecordMapper.selectById(id);
    }

    /**
     * 新增数据
     *
     * @param shareActivityRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareActivityRecord insert(ShareActivityRecord shareActivityRecord) {
        this.shareActivityRecordMapper.insert(shareActivityRecord);
        return shareActivityRecord;
    }

    /**
     * 修改数据
     *
     * @param shareActivityRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ShareActivityRecord shareActivityRecord) {
        return this.shareActivityRecordMapper.updateById(shareActivityRecord);

    }

    /**
     * 1、判断是否分享过 2、生成分享记录 3、加密scene 4、调起微信
     */
    @Override
    public R generateSharePicture(Integer activityId, String page, String envVersion) {

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("order  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //限频
        boolean result = redisService.setNx(CacheConstant.SHARE_ACTIVITY_UID + user.getUid(), "1", 5 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        // 获取小程序appId
        // 只使用 merchantMinProAppId、merchantMinProAppSecert 参数，可以调用此方法，加盟商ID传入默认 0
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryPreciseCacheByTenantIdAndFranchiseeId(tenantId, MultiFranchiseeConstant.DEFAULT_FRANCHISEE);
        if (Objects.isNull(electricityPayParams)) {
            log.warn("CREATE MEMBER_ORDER ERROR ,NOT FOUND PAY_PARAMS");
            return R.failMsg("未配置支付参数!");
        }

        //参数page
        if (Objects.isNull(page)) {
            page = "pages/start/index";
        }

        //1、判断是否分享过
        ShareActivityRecord oldShareActivityRecord = shareActivityRecordMapper
                .selectOne(new LambdaQueryWrapper<ShareActivityRecord>().eq(ShareActivityRecord::getUid, user.getUid()).eq(ShareActivityRecord::getActivityId, activityId));

        //第一次分享
        if (Objects.isNull(oldShareActivityRecord)) {
            //2、生成分享记录
            //2.1 、生成code
            String code = RandomUtil.randomNumbers(6);

            //2.2、生成分享记录
            ShareActivityRecord shareActivityRecord = new ShareActivityRecord();
            shareActivityRecord.setActivityId(activityId);
            shareActivityRecord.setUid(user.getUid());
            shareActivityRecord.setTenantId(tenantId);
            shareActivityRecord.setCode(code);
            shareActivityRecord.setCreateTime(System.currentTimeMillis());
            shareActivityRecord.setUpdateTime(System.currentTimeMillis());
            shareActivityRecord.setStatus(ShareActivityRecord.STATUS_INIT);

            Integer franchiseeId = getFranchiseeId(activityId);
            if (Objects.nonNull(franchiseeId)) {
                shareActivityRecord.setFranchiseeId(franchiseeId.longValue());
            }

            shareActivityRecordMapper.insert(shareActivityRecord);
        }

        ShareActivityRecord shareActivityRecord = shareActivityRecordMapper
                .selectOne(new LambdaQueryWrapper<ShareActivityRecord>().eq(ShareActivityRecord::getUid, user.getUid()).eq(ShareActivityRecord::getActivityId, activityId));

        //3、scene
        String scene = "uid:" + user.getUid() + ",id:" + activityId + ",type:1";

        //修改分享状态
        ShareActivityRecord newShareActivityRecord = new ShareActivityRecord();
        newShareActivityRecord.setId(shareActivityRecord.getId());
        newShareActivityRecord.setUpdateTime(System.currentTimeMillis());

        //4、调起微信
        SharePicture sharePicture = new SharePicture();
        sharePicture.setPage(page);
        sharePicture.setScene(scene);
        sharePicture.setAppId(electricityPayParams.getMerchantMinProAppId());
        sharePicture.setAppSecret(electricityPayParams.getMerchantMinProAppSecert());
        if (StringUtils.isNotBlank(envVersion)) {
            sharePicture.setEnvVersion(envVersion);
        }
        Pair<Boolean, Object> getShareUrlPair = generateShareUrlService.generateSharePicture(sharePicture);

        //分享失败
        if (!getShareUrlPair.getLeft()) {
            newShareActivityRecord.setStatus(ShareActivityRecord.STATUS_FAIL);
            shareActivityRecordMapper.updateById(newShareActivityRecord);
            return R.fail(getShareUrlPair.getRight());
        }

        //分享成功
        newShareActivityRecord.setStatus(ShareActivityRecord.STATUS_SUCCESS);
        shareActivityRecordMapper.updateById(newShareActivityRecord);
        return R.ok(getShareUrlPair.getRight());

    }

    private Integer getFranchiseeId(Integer activityId) {
        ShareActivity shareActivity = shareActivityService.queryByIdFromCache(activityId);
        if (Objects.isNull(shareActivity) || Objects.isNull(shareActivity.getFranchiseeId())) {
            return null;
        }
        return shareActivity.getFranchiseeId();
    }

    @Override
    @Slave
    public ShareActivityRecord queryByUid(Long uid, Integer activityId) {
        return shareActivityRecordMapper
                .selectOne(new LambdaQueryWrapper<ShareActivityRecord>().eq(ShareActivityRecord::getUid, uid).eq(ShareActivityRecord::getActivityId, activityId));
    }

    @Override
    public void addCountByUid(Long uid, Integer activityId) {
        shareActivityRecordMapper.addCountByUid(uid, activityId);
    }

    @Override
    public void reduceAvailableCountByUid(Long uid, Integer count, Integer activityId) {
        shareActivityRecordMapper.reduceAvailableCountByUid(uid, count, activityId);
    }

    @Override
    @Slave
    public R queryList(ShareActivityRecordQuery shareActivityRecordQuery) {
        List<ShareActivityRecordVO> shareActivityRecordVOList = shareActivityRecordMapper.queryList(shareActivityRecordQuery);
        //获取用户领取的优惠券数量
        for (ShareActivityRecordVO shareActivityRecordVO : shareActivityRecordVOList) {
            shareActivityRecordVO.setCouponCount(getReceivedCouponCount(shareActivityRecordVO));

            Long franchiseeId = shareActivityRecordVO.getFranchiseeId();
            if (Objects.nonNull(franchiseeId)) {
                shareActivityRecordVO.setFranchiseeName(Optional.ofNullable(franchiseeService.queryByIdFromCache(franchiseeId)).map(Franchisee::getName).orElse(StringUtils.EMPTY));
            }
            if (Objects.isNull(shareActivityRecordVO.getFranchiseeName())){
                Integer id = getFranchiseeId(shareActivityRecordVO.getActivityId());
                if (Objects.nonNull(id)){
                    shareActivityRecordVO.setFranchiseeName(Optional.ofNullable(franchiseeService.queryByIdFromCache(Long.valueOf(id))).map(Franchisee::getName).orElse(StringUtils.EMPTY));
                }
            }
        }
        return R.ok(shareActivityRecordVOList);
    }

    private int getReceivedCouponCount(ShareActivityRecordVO shareActivityRecordVO) {
        int couponCount = 0;
        ShareActivity shareActivity = shareActivityService.queryByIdWithDelFlag(shareActivityRecordVO.getActivityId());
        Long uid = shareActivityRecordVO.getUid();

        if (Objects.isNull(shareActivity)) {
            return couponCount;
        }
        List<ShareActivityRule> shareActivityRuleList = shareActivityRuleService.queryByActivityIdWithDelFlag(shareActivity.getId());
        if (CollectionUtils.isEmpty(shareActivityRuleList)){
            return couponCount;
        }
        if (ShareActivity.RECEIVE_TYPE_CYCLE.equals(shareActivity.getReceiveType())) {
            //循环领取的领取规则有且仅有一个
            ShareActivityRule shareActivityRule = shareActivityRuleList.get(0);
            if (CollectionUtils.isNotEmpty(shareActivityRule.getCoupons())) {
                Integer count = userCouponService.queryTheVoucherHasBeenCollected(shareActivity.getId(), shareActivityRule.getId(), uid, shareActivityRule.getCoupons());
                couponCount = Objects.isNull(count) ? 0 : count;
            }
//            Coupon coupon = couponService.queryByIdFromCache(shareActivityRule.getCouponId());
//            if (Objects.nonNull(coupon)) {
//                List<UserCoupon> userCoupons = userCouponService.selectListByActivityIdAndCouponId(shareActivity.getId(), shareActivityRule.getId(), coupon.getId(), uid);
//                if (Objects.nonNull(userCoupons)) {
//                    couponCount = userCoupons.size();
//                }
//            }
        } else {
            for (ShareActivityRule shareActivityRule : shareActivityRuleList) {
                if (CollectionUtils.isNotEmpty(shareActivityRule.getCoupons())) {
                    Integer count = userCouponService.queryTheVoucherHasBeenCollected(shareActivity.getId(), shareActivityRule.getId(), uid, shareActivityRule.getCoupons());
                    couponCount += Objects.isNull(count) ? 0 : count;
                }
//                Integer couponId = shareActivityRule.getCouponId();
//                Coupon coupon = couponService.queryByIdFromCache(couponId);
//                if (Objects.nonNull(coupon)) {
//                    UserCoupon userCoupon = userCouponService.queryByActivityIdAndCouponId(shareActivity.getId(), shareActivityRule.getId(), coupon.getId(), uid);
//                    if (Objects.nonNull(userCoupon)) {
//                        couponCount = couponCount + 1;
//                    }
//                }
            }

        }

        return couponCount;

    }

    @Override
    @Slave
    public R queryCount(ShareActivityRecordQuery shareActivityRecordQuery) {
        return R.ok(shareActivityRecordMapper.queryCount(shareActivityRecordQuery));
    }

    @Override
    public void shareActivityRecordExportExcel(ShareActivityRecordQuery shareActivityRecordQuery, HttpServletResponse response) {
        if (Objects.isNull(shareActivityRecordQuery.getEndTime()) || Objects.isNull(shareActivityRecordQuery.getStartTime())) {
            throw new CustomBusinessException("请选择开始日期和结束日期");
        }

        //只能导出30天
        int limitDay = (int) ((shareActivityRecordQuery.getEndTime() - shareActivityRecordQuery.getStartTime()) / (3600000 * 24));
        if (limitDay > 30 || limitDay < 0) {
            throw new CustomBusinessException("日期不合法请重新选择");
        }

        Triple<List<Long>, List<Long>, Boolean> triple = assertPermissionService.assertPermissionByTriple(SecurityUtils.getUserInfo());
        if (triple.getRight()) {
            shareActivityRecordQuery.setFranchiseeIds(triple.getLeft());
            shareActivityRecordQuery.setStoreIds(triple.getMiddle());
        }

        List<ShareActivityRecordVO> shareActivityRecordVOList = shareActivityRecordMapper.queryList(shareActivityRecordQuery);
        if (CollectionUtils.isEmpty(shareActivityRecordVOList)) {
            throw new CustomBusinessException("查不到邀请活动记录");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        List<ShareActivityRecordExcelVO> voList = new ArrayList<>();
        shareActivityRecordVOList.parallelStream().forEachOrdered(item -> {
            ShareActivityRecordExcelVO vo = new ShareActivityRecordExcelVO();
            BeanUtils.copyProperties(item, vo);

            date.setTime(item.getCreateTime());
            vo.setCreateTime(sdf.format(date));
            //vo.setStatus(getStatusName(item.getStatus()));

            Integer couponCount = getReceivedCouponCount(item);
            vo.setCouponCount(String.valueOf(couponCount));

            voList.add(vo);
        });

        String fileName = "邀请活动记录报表.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, ShareActivityRecordExcelVO.class).sheet("sheet").doWrite(voList);
            return;
        } catch (IOException e) {
            log.error("导出报表失败！", e);
        }
    }

    @Override
    public R getQrCodeShareParam(Integer activityId) {

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("order  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(tenant) || StringUtils.isBlank(tenant.getCode())) {
            log.warn("INVITATION ACTIVITY WARN! tenant is null,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //限频
        boolean result = redisService.setNx(CacheConstant.SHARE_ACTIVITY_UID + user.getUid(), "1", 5 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //1、判断是否分享过
        ShareActivityRecord oldShareActivityRecord = shareActivityRecordMapper
                .selectOne(new LambdaQueryWrapper<ShareActivityRecord>().eq(ShareActivityRecord::getUid, user.getUid()).eq(ShareActivityRecord::getActivityId, activityId));

        //第一次分享
        if (Objects.isNull(oldShareActivityRecord)) {
            //2、生成分享记录
            //2.1 、生成code
            String code = RandomUtil.randomNumbers(6);

            //2.2、生成分享记录
            ShareActivityRecord shareActivityRecord = new ShareActivityRecord();
            shareActivityRecord.setActivityId(activityId);
            shareActivityRecord.setUid(user.getUid());
            shareActivityRecord.setTenantId(tenantId);
            shareActivityRecord.setCode(code);
            shareActivityRecord.setCreateTime(System.currentTimeMillis());
            shareActivityRecord.setUpdateTime(System.currentTimeMillis());
            Integer franchiseeId = this.getFranchiseeId(activityId);
            if (Objects.nonNull(franchiseeId)) {
                shareActivityRecord.setFranchiseeId(franchiseeId.longValue());
            }
            // 直接成功，小程序端生成不存在失败
            shareActivityRecord.setStatus(ShareActivityRecord.STATUS_SUCCESS);
            shareActivityRecordMapper.insert(shareActivityRecord);
        }
        //3、scene
        String scene = "uid:" + user.getUid() + ",id:" + activityId + ",type:1";

        QrCodeParamVO qrCodeParamVO = new QrCodeParamVO();
        qrCodeParamVO.setScene(scene);
        qrCodeParamVO.setTenantCode(tenant.getCode());
        qrCodeParamVO.setPhone(user.getPhone());

        return R.ok(qrCodeParamVO);

    }


    private String getStatusName(Integer status) {
        //1--初始化，2--已分享，3--分享失败
        String statusName = "";
        if (Objects.equals(status, ShareActivityRecord.STATUS_INIT)) {
            statusName = "初始化";
        } else if (Objects.equals(status, ShareActivityRecord.STATUS_SUCCESS)) {
            statusName = "已分享";
        } else if (Objects.equals(status, ShareActivityRecord.STATUS_FAIL)) {
            statusName = "分享失败";
        }

        return statusName;
    }

}
