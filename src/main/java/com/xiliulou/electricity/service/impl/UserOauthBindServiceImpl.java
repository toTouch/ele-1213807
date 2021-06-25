package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.mapper.UserOauthBindMapper;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.web.query.OauthBindQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * (UserOauthBind)表服务实现类
 *
 * @author makejava
 * @since 2020-12-03 09:17:39
 */
@Service("userOauthBindService")
@Slf4j
public class UserOauthBindServiceImpl implements UserOauthBindService {
    @Resource
    private UserOauthBindMapper userOauthBindMapper;
    @Autowired
    private UserService userService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserOauthBind queryByIdFromDB(Long id) {
        return this.userOauthBindMapper.selectById(id);
    }


    /**
     * 新增数据
     *
     * @param userOauthBind 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserOauthBind insert(UserOauthBind userOauthBind) {
        this.userOauthBindMapper.insert(userOauthBind);
        return userOauthBind;
    }

    /**
     * 修改数据
     *
     * @param userOauthBind 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(UserOauthBind userOauthBind) {
        return this.userOauthBindMapper.updateById(userOauthBind);

    }


    @Override
    public UserOauthBind queryOauthByOpenIdAndSource(String openid, int source) {
        return this.userOauthBindMapper.selectOne(new LambdaQueryWrapper<UserOauthBind>().eq(UserOauthBind::getThirdId, openid).eq(UserOauthBind::getSource, source));
    }

    @Override
    public UserOauthBind queryByUserPhone(String phone, int source) {
        return this.userOauthBindMapper.selectOne(new LambdaQueryWrapper<UserOauthBind>().eq(UserOauthBind::getPhone, phone).eq(UserOauthBind::getSource, source).eq(UserOauthBind::getStatus, UserOauthBind.STATUS_BIND));
    }

    @Override
    @DS(value = "slave_1")
    public Pair<Boolean, Object> queryListByCondition(Integer size, Integer offset, Long uid, String thirdId, String phone) {
        List<UserOauthBind> list = this.userOauthBindMapper.queryListByCondition(size, offset, uid, thirdId, phone);
        return Pair.of(true, list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> updateOauthBind(OauthBindQuery oauthBindQuery) {
        UserOauthBind userOauthBind = queryByIdFromDB(oauthBindQuery.getId());
        if (Objects.isNull(userOauthBind)) {
            return Pair.of(false, "查询不到第三方账号！");
        }

        UserOauthBind build = UserOauthBind
                .builder()
                .status(oauthBindQuery.getStatus())
                .phone(oauthBindQuery.getPhone())
                .thirdId(oauthBindQuery.getThirdId())
                .id(oauthBindQuery.getId())
                .build();

        if (Objects.nonNull(oauthBindQuery.getStatus())) {
            User user = userService.queryByUidFromCache(userOauthBind.getUid());
            User updateUser = User.builder()
                    .updateTime(System.currentTimeMillis())
                    .lockFlag(oauthBindQuery.getStatus() - 1)
                    .uid(user.getUid())
                    .build();
            userService.updateUser(updateUser, user);
        }

        return update(build) == 1 ? Pair.of(true, null) : Pair.of(false, "修改失败 ");
    }

    /**
     * @param uid
     * @return
     */
    @Override
    public UserOauthBind queryUserOauthBySysId(Long uid) {


        return   userOauthBindMapper.queryUserOauthBySysId(uid);
    }
}
