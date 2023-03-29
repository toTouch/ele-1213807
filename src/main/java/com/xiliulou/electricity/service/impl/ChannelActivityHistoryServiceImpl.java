package com.xiliulou.electricity.service.impl;

import com.alibaba.excel.EasyExcel;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.CarMemberCardOrder;
import com.xiliulou.electricity.entity.ChannelActivity;
import com.xiliulou.electricity.entity.ChannelActivityHistory;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserChannel;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.ChannelActivityHistoryMapper;
import com.xiliulou.electricity.service.CarMemberCardOrderService;
import com.xiliulou.electricity.service.ChannelActivityHistoryService;
import com.xiliulou.electricity.service.ChannelActivityService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserChannelService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.AESUtils;
import com.xiliulou.electricity.utils.DesensitizationUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ChannelActivityCodeVo;
import com.xiliulou.electricity.vo.ChannelActivityHistoryExcelVo;
import com.xiliulou.electricity.vo.ChannelActivityHistoryVo;
import com.xiliulou.electricity.vo.UserInfoExcelVO;
import org.apache.commons.collections.ArrayStack;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 * (ChannelActivityHistory)表服务实现类
 *
 * @author Hardy
 * @since 2023-03-23 09:24:25
 */
@Service
@Slf4j
public class ChannelActivityHistoryServiceImpl implements ChannelActivityHistoryService {
    
    @Resource
    private ChannelActivityHistoryMapper channelActivityHistoryMapper;
    
    @Autowired
    private UserInfoService userInfoService;
    
    @Autowired
    private UserChannelService userChannelService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ChannelActivityService channelActivityService;
    
    @Autowired
    private UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    private CarMemberCardOrderService carMemberCardOrderService;
    
    @Autowired
    private TenantService tenantService;
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ChannelActivityHistory queryByIdFromDB(Long id) {
        return this.channelActivityHistoryMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ChannelActivityHistory queryByIdFromCache(Long id) {
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
    public List<ChannelActivityHistory> queryAllByLimit(int offset, int limit) {
        return this.channelActivityHistoryMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param channelActivityHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChannelActivityHistory insert(ChannelActivityHistory channelActivityHistory) {
        this.channelActivityHistoryMapper.insertOne(channelActivityHistory);
        return channelActivityHistory;
    }
    
    /**
     * 修改数据
     *
     * @param channelActivityHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ChannelActivityHistory channelActivityHistory) {
        return this.channelActivityHistoryMapper.update(channelActivityHistory);
        
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
        return this.channelActivityHistoryMapper.deleteById(id) > 0;
    }
    
    /**
     * 查询邀请人邀请数量
     */
    @Override
    public Long queryInviteCount(Long uid) {
        return this.channelActivityHistoryMapper.queryInviteCount(uid);
    }
    
    @Override
    public ChannelActivityHistory queryByUid(Long uid) {
        return this.channelActivityHistoryMapper.queryByUid(uid);
    }
    
    @Override
    @Slave
    public Triple<Boolean, String, Object> queryList(Long size, Long offset, String phone, Long beginTime,
            Long endTime) {
        List<ChannelActivityHistoryVo> query = channelActivityHistoryMapper
                .queryList(size, offset, phone, TenantContextHolder.getTenantId(), beginTime, endTime);
        if (CollectionUtils.isEmpty(query)) {
            return Triple.of(true, "", new ArrayList<>());
        }
        
        query.forEach(item -> {
            UserInfo inviteUserInfo = userInfoService.queryByUidFromDb(item.getInviteUid());
            if (Objects.nonNull(inviteUserInfo)) {
                item.setInviteName(inviteUserInfo.getName());
                item.setInvitePhone(inviteUserInfo.getPhone());
            }
            
            UserInfo channelUserInfo = userInfoService.queryByUidFromDb(item.getChannelUid());
            if (Objects.nonNull(channelUserInfo)) {
                item.setChannelName(channelUserInfo.getName());
                item.setChannelPhone(channelUserInfo.getPhone());
            }
        });
        return Triple.of(true, "", query);
    }
    
    @Override
    @Slave
    public Triple<Boolean, String, Object> queryCount(String phone, Long beginTime, Long endTime) {
        Long count = channelActivityHistoryMapper
                .queryCount(phone, TenantContextHolder.getTenantId(), beginTime, endTime);
        return Triple.of(true, "", count);
    }
    
    @Override
    public R queryCode() {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            log.error("USER CHANNEL QUERY CODE ERROR! not found user");
            return R.fail("100001", "用户不存在");
        }
    
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("USER CHANNEL QUERY CODE ERROR! not found user");
            return R.fail("100001", "用户不存在");
        }
    
        //活动是否下架
        ChannelActivity usableActivity = channelActivityService.findUsableActivity(TenantContextHolder.getTenantId());
        if (Objects.isNull(usableActivity)) {
            log.error("USER CHANNEL SCAN ERROR! not find usableActivity! tenantId={},user={}",
                    TenantContextHolder.getTenantId(), uid);
            return R.fail("100458", "渠道活动未开启");
        }
        
        ChannelActivityHistory channelActivityHistory = this.queryByUid(uid);
        if (Objects.nonNull(channelActivityHistory)) {
            String code = codeEnCoder(ChannelActivityCodeVo.TYPE_INVITE, uid, channelActivityHistory.getChannelUid());
            String phone = null;
            String tenantCode = null;
    
            User user = userService.queryByUidFromCache(channelActivityHistory.getInviteUid());
            if (Objects.nonNull(user)) {
                phone = DesensitizationUtil.phoneDesensitization(user.getPhone());
            }
    
            Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
            if (Objects.nonNull(tenant)) {
                tenantCode = tenant.getCode();
            }
    
            return R.ok(new ChannelActivityCodeVo(code, phone, ChannelActivityCodeVo.TYPE_INVITE, tenantCode));
        }
        
        UserChannel userChannel = userChannelService.queryByUidFromCache(uid);
        if (Objects.nonNull(userChannel)) {
            String code = codeEnCoder(ChannelActivityCodeVo.TYPE_CHANNEL, uid, uid);
            String phone = null;
            String tenantCode = null;
    
            User user = userService.queryByUidFromCache(userChannel.getOperateUid());
            if (Objects.nonNull(user)) {
                phone = DesensitizationUtil.phoneDesensitization(user.getPhone());
            }
    
            Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
            if (Objects.nonNull(tenant)) {
                tenantCode = tenant.getCode();
            }
    
            return R.ok(new ChannelActivityCodeVo(code, phone, ChannelActivityCodeVo.TYPE_CHANNEL, tenantCode));
        }
        
        log.warn("USER CHANNEL QUERY CODE ERROR! user not partake activity! uid={}", uid);
        return R.fail("100456", "用户未参与渠道人活动");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R scanIntoActivity(String code) {
        if (StringUtils.isBlank(code)) {
            return R.ok();
        }
        
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            log.error("USER CHANNEL QUERY CODE ERROR! not found user");
            return R.fail("100001", "用户不存在");
        }
    
        if (!redisService.setNx(CacheConstant.CACHE_SCAN_INTO_ACTIVITY_LOCK + uid, "ok", 3000L, false)) {
            log.warn("USER CHANNEL QUERY CODE ERROR! Frequency too fast");
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
    
        User user = userService.queryByUidFromCache(uid);
        if (Objects.isNull(user)) {
            log.error("USER CHANNEL QUERY CODE ERROR! not found user, uid={}", uid);
            return R.fail("100001", "用户不存在");
        }
    
        //活动是否下架
        ChannelActivity usableActivity = channelActivityService.findUsableActivity(TenantContextHolder.getTenantId());
        if (Objects.isNull(usableActivity)) {
            log.error("USER CHANNEL SCAN ERROR! not find usableActivity! tenantId={},user={}",
                    TenantContextHolder.getTenantId(), uid);
            return R.fail("100458", "渠道活动未开启");
        }
    
        String decrypt = codeDeCoder(code);
        if (StringUtils.isBlank(decrypt)) {
            log.error("USER CHANNEL SCAN ERROR! code decrypt error! code={}, user={}", code, uid);
            return R.fail("100457", "渠道活动二维码解码失败");
        }
        
        String[] split = decrypt.split(":");
        if (split.length != 3) {
            log.error("USER CHANNEL SCAN ERROR! code length illegal! code={}, user={}", code, uid);
            return R.fail("100459", "渠道活动二维码内容不合法");
        }
    
        Integer type = null;
        Long inviteUid = null;
        Long channelUid = null;
    
        try {
            type = Integer.parseInt(split[0]);
            inviteUid = Long.parseLong(split[1]);
            channelUid = Long.parseLong(split[2]);
        } catch (Exception e) {
            log.error("USER CHANNEL SCAN ERROR! code parse error!", e);
        }
    
        if (Objects.isNull(type) || Objects.isNull(inviteUid) || Objects.isNull(channelUid)) {
            log.error("USER CHANNEL SCAN ERROR! code parse error! decrypt={}, user={}", decrypt, uid);
            return R.fail("100459", "渠道活动二维码内容不合法");
        }
    
        // 是否自己扫自己的码，
        if (Objects.equals(uid, inviteUid)) {
            return R.ok();
        }
    
        //类型是否一致，
        if (!Objects.equals(type, ChannelActivityCodeVo.TYPE_CHANNEL) && !Objects
                .equals(type, ChannelActivityCodeVo.TYPE_INVITE)) {
            log.error("USER CHANNEL SCAN ERROR! code type error! type={}, user={}", type, uid);
            return R.fail("100459", "渠道活动二维码内容不合法");
        }
    
        //用户是否存在，
        User inviteUser = userService.queryByUidFromCache(inviteUid);
        if (Objects.isNull(inviteUser)) {
            log.error("USER CHANNEL SCAN ERROR! inviteUser not find error! user={}", inviteUid);
            return R.fail("100001", "邀请用户用户不存在");
        }
    
        User channelUser = userService.queryByUidFromCache(channelUid);
        if (Objects.isNull(channelUser)) {
            log.error("USER CHANNEL SCAN ERROR! channelUser not find error! user={}", channelUid);
            return R.fail("100001", "渠道人用户不存在");
        }
        
        // 是否参与过邀请活动，
        ChannelActivityHistory channelActivityHistory = this.queryByUid(uid);
        if (Objects.nonNull(channelActivityHistory)) {
            log.error("USER CHANNEL SCAN ERROR! user has participated in activities! user={}", uid);
            return R.ok();
        }
    
        // 是否渠道人，
        UserChannel userChannel = userChannelService.queryByUidFromCache(uid);
        if (Objects.nonNull(userChannel)) {
            log.error("USER CHANNEL SCAN ERROR! user is channel user! user={}", uid);
            return R.ok();
        }
        
        // 是否购买过套餐
        if (userBuyMemberCardCheck(uid)) {
            log.error("USER CHANNEL SCAN ERROR! user has buy memberCard ! user={}", uid);
            return R.fail("100462", "您是老用户，无法参加渠道活动");
        }
    
        ChannelActivityHistory saveChannelActivityHistory = new ChannelActivityHistory();
        saveChannelActivityHistory.setUid(uid);
        saveChannelActivityHistory.setInviteUid(inviteUid);
        saveChannelActivityHistory.setChannelUid(channelUid);
        saveChannelActivityHistory.setStatus(ChannelActivityHistory.STATUS_INIT);
        saveChannelActivityHistory.setCreateTime(System.currentTimeMillis());
        saveChannelActivityHistory.setUpdateTime(System.currentTimeMillis());
        saveChannelActivityHistory.setTenantId(TenantContextHolder.getTenantId());
        insert(saveChannelActivityHistory);
    
        return R.ok();
    }
    
    @Override
    @Slave
    public void queryExportExcel(String phone, Long beginTime, Long endTime, HttpServletResponse response) {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            log.error("USER CHANNEL QUERY CODE ERROR! not found user");
            throw new CustomBusinessException("未查询到用户");
        }
        
        if (Objects.isNull(beginTime) || Objects.isNull(endTime) || (endTime - beginTime) == 0) {
            log.error("USER CHANNEL EXPORT EXCEL ERROR! Illegal time ! user={}", uid);
            throw new CustomBusinessException("搜索日期不合法");
        }
        
        Double days = (Double.valueOf(endTime - beginTime)) / 1000 / 3600 / 24;
        if (days > 33) {
            throw new CustomBusinessException("搜索日期不能大于33天");
        }
        
        Long offset = 0L;
        Long size = 2000L;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        
        List<ChannelActivityHistoryVo> query = channelActivityHistoryMapper
                .queryList(size, offset, phone, TenantContextHolder.getTenantId(), beginTime, endTime);
        List<ChannelActivityHistoryExcelVo> voList = new ArrayList<>();
        
        Optional.ofNullable(query).orElse(new ArrayList<>()).forEach(item -> {
            ChannelActivityHistoryExcelVo vo = new ChannelActivityHistoryExcelVo();
            vo.setName(Objects.isNull(item.getName()) ? "" : item.getName());
            vo.setPhone(Objects.isNull(item.getPhone()) ? "" : item.getPhone());
    
            UserInfo inviteUserInfo = userInfoService.queryByUidFromDb(item.getInviteUid());
            if (Objects.nonNull(inviteUserInfo)) {
                vo.setInviterName(Objects.isNull(inviteUserInfo.getName()) ? "未实名认证" : inviteUserInfo.getName());
                vo.setInviterPhone(Objects.isNull(inviteUserInfo.getPhone()) ? "" : inviteUserInfo.getPhone());
            }
            
            UserInfo channelUserInfo = userInfoService.queryByUidFromDb(item.getChannelUid());
            if (Objects.nonNull(channelUserInfo)) {
                vo.setChannelName(Objects.isNull(channelUserInfo.getName()) ? "未实名认证" : channelUserInfo.getName());
            }
            
            date.setTime(item.getCreateTime());
            vo.setCreateTime(sdf.format(date));
            vo.setStatus(queryStatus(item.getStatus()));
            
            voList.add(vo);
        });
        
        String fileName = "渠道活动记录报表.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, ChannelActivityHistoryExcelVo.class).sheet("sheet").doWrite(voList);
            return;
        } catch (IOException e) {
            log.error("导出报表失败！", e);
        }
    }
    
    private String queryStatus(Integer status) {
        String result = "";
        switch (status) {
            case 1:
                result = "已参与";
                break;
            case 2:
                result = "邀请成功";
                break;
            case 3:
                result = "已过期";
                break;
            case 4:
                result = "被替换";
                break;
            default:
                result = "";
        }
        return result;
    }
    
    private String codeDeCoder(String code) {
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
    
    
    private String codeEnCoder(Integer type, Long uid, Long channelUid) {
        StringBuilder sb = new StringBuilder();
        sb.append(type).append(":").append(uid).append(":").append(channelUid);
        
        String encrypt = AESUtils.encrypt(sb.toString());
        if (StringUtils.isNotBlank(encrypt)) {
            Base64.Encoder encoder = Base64.getUrlEncoder();
            byte[] base64Result = encoder.encode(encrypt.getBytes());
            return new String(base64Result);
        }
        return null;
    }
    
    private boolean userBuyMemberCardCheck(Long uid) {
        boolean batteryMemberCard = true;
        boolean carMemberCard = true;
    
        //        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        //        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime())
        //                || Objects
        //                .equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)) {
        //            batteryMemberCard = false;
        //        }
    
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService
                .queryLastPayMemberCardTimeByUid(uid, null, TenantContextHolder.getTenantId());
        if (Objects.isNull(electricityMemberCardOrder)) {
            batteryMemberCard = false;
        }
    
        CarMemberCardOrder carMemberCardOrder = carMemberCardOrderService
                .queryLastPayMemberCardTimeByUid(uid, null, TenantContextHolder.getTenantId());
        if (Objects.isNull(carMemberCardOrder)) {
            carMemberCard = false;
        }
        
        return batteryMemberCard || carMemberCard;
    }
}
