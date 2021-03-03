package com.xiliulou.electricity.service.impl;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleAuthEntry;
import com.xiliulou.electricity.entity.EleUserAuth;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.EleUserAuthMapper;
import com.xiliulou.electricity.service.EleAuthEntryService;
import com.xiliulou.electricity.service.EleUserAuthService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 实名认证信息(TEleUserAuth)表服务实现类
 *
 * @author makejava
 * @since 2021-02-20 13:37:
 *
 *
 *
 *
 *
 */
@Service("eleUserAuthService")
@Slf4j
public class EleUserAuthServiceImpl implements EleUserAuthService {
    @Resource
    EleUserAuthMapper eleUserAuthMapper;

    @Autowired
    EleAuthEntryService eleAuthEntryService;

    @Autowired
    UserInfoService userInfoService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleUserAuth queryByIdFromDB(Long id) {
        return this.eleUserAuthMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleUserAuth queryByIdFromCache(Long id) {
        return null;
    }
    
    /**
     * 新增数据
     *
     * @param eleUserAuth 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleUserAuth insert(EleUserAuth eleUserAuth) {
        this.eleUserAuthMapper.insert(eleUserAuth);
        return eleUserAuth;
    }

    /**
     * 修改数据
     *
     * @param eleUserAuth 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleUserAuth eleUserAuth) {
       return this.eleUserAuthMapper.update(eleUserAuth);
         
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R insertEleUserAuthList(List<EleUserAuth> eleUserAuthList) {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        UserInfo oldUserInfo = userInfoService.queryByUid(uid);
        //用户是否可用
        if (Objects.isNull(oldUserInfo) || Objects.equals(oldUserInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELECTRICITY  ERROR! not found userInfo,uid:{} ",uid);
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        UserInfo userInfo=new UserInfo();
        userInfo.setId(oldUserInfo.getId());

        for (EleUserAuth eleUserAuth : eleUserAuthList) {
            eleUserAuth.setUid(uid);
            if (StringUtils.isEmpty(eleUserAuth.getValue())) {
                return R.fail("ELECTRICITY.0007", "不合法的参数");
            }

            EleAuthEntry eleAuthEntryDb = eleAuthEntryService.queryByIdFromDB(eleUserAuth.getEntryId());
            if (Objects.isNull(eleAuthEntryDb)) {
                log.error("not found authEntry entryId:{}", eleUserAuth.getEntryId());
                return R.fail("审核资料项不存在!");
            }

            if (ObjectUtil.equal(EleAuthEntry.ID_NAME_ID, eleUserAuth.getEntryId())) {
                userInfo.setName(eleUserAuth.getValue());
            }
            if (ObjectUtil.equal(EleAuthEntry.ID_ID_CARD, eleUserAuth.getEntryId())) {
                userInfo.setIdNumber(eleUserAuth.getValue());
            }
            if (ObjectUtil.equal(EleAuthEntry.ID_MAILBOX, eleUserAuth.getEntryId())) {
                userInfo.setMailbox(eleUserAuth.getValue());
            }

            eleUserAuth.setStatus(EleUserAuth.STATUS_PENDING_REVIEW);
            eleUserAuth.setCreateTime(System.currentTimeMillis());
            eleUserAuth.setUpdateTime(System.currentTimeMillis());
            eleUserAuthMapper.insert(eleUserAuth);
        }

        userInfo.setUid(uid);
        userInfo.setAuthStatus(UserInfo.AUTH_STATUS_PENDING_REVIEW);
        userInfo.setUpdateTime(System.currentTimeMillis());
        userInfoService.update(userInfo);

        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateEleUserAuthList(List<EleUserAuth> eleUserAuthList) {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        UserInfo oldUserInfo = userInfoService.queryByUid(uid);
        //用户是否可用
        if (Objects.isNull(oldUserInfo) || Objects.equals(oldUserInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELECTRICITY  ERROR! not found userInfo,uid:{} ",uid);
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        if(Objects.equals(oldUserInfo.getAuthStatus(),UserInfo.AUTH_STATUS_REVIEW_PASSED)){
            return R.fail("审核通过，无法修改!");
        }
        UserInfo userInfo=new UserInfo();
        userInfo.setId(oldUserInfo.getId());

        for (EleUserAuth eleUserAuth : eleUserAuthList) {
            eleUserAuth.setUid(uid);
            if (StringUtils.isEmpty(eleUserAuth.getValue())) {
                return R.fail("ELECTRICITY.0007", "不合法的参数");
            }

            EleAuthEntry eleAuthEntryDb = eleAuthEntryService.queryByIdFromCache(eleUserAuth.getEntryId());
            if (Objects.isNull(eleAuthEntryDb)) {
                log.error("not found authEntry entryId:{}", eleUserAuth.getEntryId());
                return R.fail("审核资料项不存在!");
            }

            if (ObjectUtil.equal(EleAuthEntry.ID_NAME_ID, eleUserAuth.getEntryId())) {
                userInfo.setName(eleUserAuth.getValue());
            }
            if (ObjectUtil.equal(EleAuthEntry.ID_ID_CARD, eleUserAuth.getEntryId())) {
                userInfo.setIdNumber(eleUserAuth.getValue());
            }
            if (ObjectUtil.equal(EleAuthEntry.ID_MAILBOX, eleUserAuth.getEntryId())) {
                userInfo.setMailbox(eleUserAuth.getValue());
            }

            eleUserAuth.setStatus(EleUserAuth.STATUS_PENDING_REVIEW);
            eleUserAuth.setUpdateTime(System.currentTimeMillis());
            if(Objects.isNull(eleUserAuth.getId())){
                eleUserAuth.setCreateTime(System.currentTimeMillis());
                eleUserAuthMapper.insert(eleUserAuth);
            }else {
                eleUserAuthMapper.update(eleUserAuth);
            }
        }

        userInfo.setUid(uid);
        userInfo.setAuthStatus(UserInfo.AUTH_STATUS_PENDING_REVIEW);
        userInfo.setUpdateTime(System.currentTimeMillis());
        userInfoService.update(userInfo);

        return R.ok();

    }

    @Override
    public R getEleUserAuthSpecificStatus(Long uid) {

        UserInfo userInfo = userInfoService.queryByUid(uid);
        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found userInfo! userId:{}",uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        return R.ok(userInfo.getAuthStatus());
    }

    @Override
    public R selectCurrentEleAuthEntriesList(Long uid) {
        return R.ok(eleUserAuthMapper.selectList(Wrappers.<EleUserAuth>lambdaQuery().eq(EleUserAuth::getUid,uid).eq(EleUserAuth::getDelFlag,EleUserAuth.DEL_NORMAL)));
    }

    @Override
    public R getEleUserServiceStatus(Long uid) {

        UserInfo userInfo = userInfoService.queryByUid(uid);
        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found userInfo! userId:{}",uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        return R.ok(userInfo.getServiceStatus());
    }

    @Override
    public void updateByUid(Long uid, Integer authStatus) {
        eleUserAuthMapper.updateByUid(uid,authStatus,System.currentTimeMillis());
    }
}