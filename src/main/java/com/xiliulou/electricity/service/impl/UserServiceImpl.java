package com.xiliulou.electricity.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.UserMapper;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.UserVo;
import com.xiliulou.electricity.web.query.AdminUserQuery;
import com.xiliulou.electricity.web.query.PasswordQuery;
import com.xiliulou.security.authentication.console.CustomPasswordEncoder;
import com.xiliulou.security.bean.TokenUser;
import com.xiliulou.security.constant.TokenConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * (User)表服务实现类
 *
 * @author makejava
 * @since 2020-11-27 11:19:51
 */
@Service("userService")
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    CustomPasswordEncoder customPasswordEncoder;
    @Resource
    private UserMapper userMapper;
    @Autowired
    RedisService redisService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    UserRoleService userRoleService;

    @Autowired
    CityService cityService;

    @Autowired
    ProvinceService provinceService;

    @Autowired
    FranchiseeBindElectricityBatteryService franchiseeBindElectricityBatteryService;

    @Value("${security.encode.key:xiliu&lo@u%12345}")
    private String encodeKey;

    @Autowired
    UserOauthBindService userOauthBindService;

    @Autowired
    RoleService roleService;

    @Autowired
    FranchiseeService franchiseeService;

    @Autowired
    StoreService storeService;

    @Autowired
    FranchiseeUserInfoService franchiseeUserInfoService;
    @Autowired
    private UserDataScopeService userDataScopeService;

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    @Override
    public User queryByUidFromCache(Long uid) {
        User cacheUser = redisService.getWithHash(CacheConstant.CACHE_USER_UID + uid, User.class);
        if (Objects.nonNull(cacheUser)) {
            return cacheUser;
        }

        User user = userMapper.selectById(uid);
        if (Objects.isNull(user)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.CACHE_USER_UID + uid, user);
        redisService.saveWithHash(CacheConstant.CACHE_USER_PHONE + user.getPhone() + ":" + user.getUserType(), user);

        return user;
    }

    /**
     * 新增数据
     *
     * @param user 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DS("master")
    public User insert(User user) {
        int insert = this.userMapper.insert(user);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            redisService.saveWithHash(CacheConstant.CACHE_USER_UID + user.getUid(), user);
            redisService.saveWithHash(CacheConstant.CACHE_USER_PHONE + user.getPhone() + ":" + user.getUserType(), user);
            return user;
        });

        return user;
    }

    /**
     * 修改数据
     *
     * @param user 实例对象
     * @return 实例对象
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer update(User user) {
        return this.userMapper.updateById(user);

    }

    /**
     * 通过主键删除数据
     *
     * @param uid 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long uid) {
//        User updateUser = new User();
//        updateUser.setUid(uid);
//        updateUser.setDelFlag(User.DEL_DEL);
//        return this.userMapper.updateUserById(updateUser)>0;
        return this.userMapper.deleteById(uid) > 0;
    }

    @Override
    public User queryByUserName(String username) {
        return this.userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getName, username).eq(User::getDelFlag, User.DEL_NORMAL));
    }

    @Override
    public User queryByUserNameAndTenantId(String username, Integer tenantId) {
        return this.userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getName, username).eq(User::getDelFlag, User.DEL_NORMAL).eq(User::getTenantId, tenantId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> addAdminUser(AdminUserQuery adminUserQuery) {
        TokenUser tokenUser = SecurityUtils.getUserInfo();
        if (Objects.isNull(tokenUser)) {
            return Triple.of(false, "USER.0001", "查询不到用户！");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        if (!Objects.equals(tokenUser.getType(), User.TYPE_USER_SUPER) && Objects.equals(adminUserQuery.getUserType(), User.TYPE_USER_SUPER)) {
            return Triple.of(false, null, "无权限操作！");
        }

        User phoneUserExists = queryByUserPhone(adminUserQuery.getPhone(), adminUserQuery.getUserType(), tenantId);
        if (Objects.nonNull(phoneUserExists)) {
            return Triple.of(false, null, "手机号已存在");
        }

        User userNameExists = queryByUserName(adminUserQuery.getName());
        if (Objects.nonNull(userNameExists)) {
            return Triple.of(false, null, "用户名已存在");
        }

        //解密密码
        String encryptPassword = adminUserQuery.getPassword();
        String decryptPassword = decryptPassword(encryptPassword);
        if (StrUtil.isEmpty(decryptPassword)) {
            log.error("ADMIN USER ERROR! decryptPassword error! username={},phone={},password={}", adminUserQuery.getName(), adminUserQuery.getPhone(), adminUserQuery.getPassword());
            return Triple.of(false, "SYSTEM.0001", "系统错误!");
        }

        //处理城市和省份
        City city = cityService.queryByIdFromDB(adminUserQuery.getCityId());
        Province province = provinceService.queryByIdFromDB(adminUserQuery.getProvinceId());

        User user = User.builder()
                .avatar("")
                .salt("")
                .createTime(System.currentTimeMillis())
                .delFlag(User.DEL_NORMAL)
                .lockFlag(User.USER_UN_LOCK)
                .gender(adminUserQuery.getGender())
                .lang(adminUserQuery.getLang())
                .loginPwd(customPasswordEncoder.encode(decryptPassword))
                .name(adminUserQuery.getName())
                .phone(adminUserQuery.getPhone())
                .updateTime(System.currentTimeMillis())
                .userType(User.TYPE_USER_NORMAL_ADMIN)
                .dataType(adminUserQuery.getDataType())
                .salt("")
                .city(Objects.nonNull(city) ? city.getName() : null)
                .province(Objects.nonNull(province) ? province.getName() : null)
                .cid(Objects.nonNull(city) ? city.getId() : null)
                .tenantId(tenantId)
                .build();
        User insert = insert(user);

//        //默认值
//        Long roleId = adminUserQuery.getUserType().longValue() + 1;
//        //运营商
//        if (Objects.equals(adminUserQuery.getUserType(), User.TYPE_USER_OPERATE)) {
//            Long role = roleService.queryByName("OPERATE_USER", tenantId);
//            if (Objects.nonNull(role)) {
//                roleId = role;
//            }
//        }
//
//        //加盟商
//        if (Objects.equals(adminUserQuery.getUserType(), User.TYPE_USER_FRANCHISEE)) {
//            Long role = roleService.queryByName("FRANCHISEE_USER", tenantId);
//            if (Objects.nonNull(role)) {
//                roleId = role;
//            }
//        }
//
//        //门店
//        if (Objects.equals(adminUserQuery.getUserType(), User.TYPE_USER_STORE)) {
//            Long role = roleService.queryByName("STORE_USER", tenantId);
//            if (Objects.nonNull(role)) {
//                roleId = role;
//            }
//        }

        //设置角色
        UserRole userRole = new UserRole();
        userRole.setRoleId(adminUserQuery.getRoleId());
        userRole.setUid(insert.getUid());
        userRoleService.insert(userRole);


        //保存用户数据范围
        if(CollectionUtils.isNotEmpty(adminUserQuery.getDataIdList())){
            List<UserDataScope> userDataScopes=buildUserDataScope(insert.getUid(),adminUserQuery.getDataIdList());
            userDataScopeService.batchInsert(userDataScopes);
        }

        return insert.getUid() != null ? Triple.of(true, null, null) : Triple.of(false, null, "保存失败!");
    }

    private List<UserDataScope> buildUserDataScope(Long uid, List<Long> dataIdList) {
        List<UserDataScope> list = Lists.newArrayList();
        dataIdList.forEach(item->{
            UserDataScope userDataScope = new UserDataScope();
            userDataScope.setUid(uid);
            userDataScope.setDataId(item);
            list.add(userDataScope);
        });

        return list;
    }

    @Override
    public String decryptPassword(String encryptPassword) {
        AES aes = new AES(Mode.CBC, Padding.ZeroPadding, new SecretKeySpec(encodeKey.getBytes(), "AES"),
                new IvParameterSpec(encodeKey.getBytes()));
        return new String(aes.decrypt(Base64.decode(encryptPassword.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
    }

    @Override
    public List<User> queryByTenantIdAndType(Integer tenantId, Integer userType) {
        return this.userMapper.selectList(new QueryWrapper<User>().eq("tenant_id", tenantId).eq("user_type", userType));
    }

    @Override
    public User queryByUserPhone(String phone, Integer type, Integer tenantId) {
        User cacheUser = redisService.getWithHash(CacheConstant.CACHE_USER_PHONE + tenantId + phone + ":" + type, User.class);
        if (Objects.nonNull(cacheUser)) {
            return cacheUser;
        }

        User user = this.userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone).eq(User::getUserType, type).eq(User::getDelFlag, User.DEL_NORMAL).eq(User::getTenantId, tenantId));
        if (Objects.isNull(user)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.CACHE_USER_UID + user.getUid(), user);
        redisService.saveWithHash(CacheConstant.CACHE_USER_PHONE + tenantId + user.getPhone() + ":" + user.getUserType(), user);

        return user;
    }

    @Override
    @DS("slave_1")
    public Pair<Boolean, Object> queryListUser(Long uid, Long size, Long offset, String name, String phone, Integer type, Long startTime, Long endTime, Integer tenantId) {
        List<User> userList = this.userMapper.queryListUserByCriteria(uid, size, offset, name, phone, type, startTime, endTime, tenantId);
        if(CollectionUtils.isEmpty(userList)){
            return Pair.of(true, Collections.EMPTY_LIST);
        }

        return Pair.of(true, userList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> updateAdminUser(AdminUserQuery adminUserQuery) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        User user = queryByUidFromCache(adminUserQuery.getUid());
        if (Objects.isNull(user)) {
            return Pair.of(false, "uid:" + adminUserQuery.getUid() + "用户不存在!");
        }

        if (!Objects.equals(user.getTenantId(), tenantId)) {
            return Pair.of(true, null);
        }

        if (StrUtil.isNotEmpty(adminUserQuery.getPhone())) {
            User phone = queryByUserPhone(adminUserQuery.getPhone(), User.TYPE_USER_NORMAL_ADMIN, user.getTenantId());
            if (Objects.nonNull(phone) && !Objects.equals(phone.getUid(), adminUserQuery.getUid())) {
                return Pair.of(false, "手机号已存在！无法修改!");
            }
        }

        if (StrUtil.isNotEmpty(adminUserQuery.getName())) {
            User nameUser = queryByUserName(adminUserQuery.getName());
            if (Objects.nonNull(nameUser) && !Objects.equals(nameUser.getUid(), adminUserQuery.getUid())) {
                return Pair.of(false, "用户名已经存在！无法修改！");
            }
        }

        String decryptPassword = null;
        if (StrUtil.isNotEmpty(adminUserQuery.getPassword())) {
            //解密密码
            String encryptPassword = adminUserQuery.getPassword();
            decryptPassword = decryptPassword(encryptPassword);
            if (StrUtil.isEmpty(decryptPassword)) {
                log.error("ADMIN USER ERROR! decryptPassword error! username={},phone={},password={}", adminUserQuery.getName(), adminUserQuery.getPhone(), adminUserQuery.getPassword());
                return Pair.of(false, "系统错误!");
            }
        }

        User updateUser = User.builder()
                .uid(user.getUid())
                .delFlag(User.DEL_NORMAL)
                .lockFlag(User.USER_UN_LOCK)
                .gender(adminUserQuery.getGender())
                .lang(adminUserQuery.getLang())
                .loginPwd(StrUtil.isEmpty(decryptPassword) ? null : customPasswordEncoder.encode(decryptPassword))
                .name(adminUserQuery.getName())
                .phone(adminUserQuery.getPhone())
                .updateTime(System.currentTimeMillis())
//                .userType(adminUserQuery.getUserType())
                .dataType(adminUserQuery.getDataType())
                .lockFlag(adminUserQuery.getLock())
                .build();

        if (Objects.nonNull(adminUserQuery.getCityId())) {
            //城市
            City city = cityService.queryByIdFromDB(adminUserQuery.getCityId());
            if (Objects.nonNull(city)) {
                Province province = provinceService.queryByIdFromDB(city.getPid());
                if (Objects.nonNull(province)) {
                    updateUser.setCid(adminUserQuery.getCityId());
                    updateUser.setCity(city.getName());
                    updateUser.setProvince(province.getName());
                }
            }
        }

        int i = updateUser(updateUser, user);
        //更新userInfo
        if (i > 0) {
            UserInfo oldUserInfo = userInfoService.queryByUidFromCache(user.getUid());
            if (Objects.nonNull(oldUserInfo)) {
                UserInfo userInfo = new UserInfo();
                userInfo.setId(oldUserInfo.getId());
                userInfo.setTenantId(tenantId);
                userInfo.setUpdateTime(System.currentTimeMillis());
                userInfo.setPhone(updateUser.getPhone());
                userInfo.setUid(oldUserInfo.getUid());
                userInfoService.update(userInfo);
            }
    
            
            //更新用户数据范围
            if (CollectionUtils.isNotEmpty(adminUserQuery.getDataIdList())) {
                userDataScopeService.deleteByUid(user.getUid());
                
                List<UserDataScope> userDataScopes = buildUserDataScope(user.getUid(), adminUserQuery.getDataIdList());
                userDataScopeService.batchInsert(userDataScopes);
            }
        }

        return i > 0 ? Pair.of(true, null) : Pair.of(false, "更新失败!");
    }

    @Override
    public Pair<Boolean, Object> deleteAdminUser(Long uid) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        User user = queryByUidFromCache(uid);
        if (Objects.isNull(user)) {
            return Pair.of(false, "uid:" + uid + "用户不存在!");
        }

        if (!Objects.equals(user.getTenantId(), tenantId)) {
            return Pair.of(true, null);
        }

        if (Objects.equals(user.getUserType(), User.TYPE_USER_NORMAL_WX_PRO)) {
            return Pair.of(false, "非法操作");
        }

        //不让删除租户
        if (Objects.equals(SecurityUtils.getUid(), 1)
                && !Objects.equals(user.getTenantId(), 1)
                && Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            return Pair.of(false, "非法操作");
        }

        //加盟商用户删除查看是否绑定普通用户，绑定普通用户则不让删除
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {

            Integer count = franchiseeService.queryByFanchisee(user.getUid());
            if (count > 0) {
                return Pair.of(false, "加盟商用户已绑定门店或用户");
            }
        }

        //门店用户删除查看是否绑定换电柜
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {

            Integer count = storeService.queryCountByFranchisee(user.getUid());
            if (count > 0) {
                return Pair.of(false, "门店用户已绑定换电柜");
            }
        }

        if (deleteById(uid)) {
            redisService.delete(CacheConstant.CACHE_USER_UID + uid);
            redisService.delete(CacheConstant.CACHE_USER_PHONE + user.getPhone() + ":" + user.getUserType());

            //删除加盟商或门店
            if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
                franchiseeService.deleteByUid(uid);
            }
            if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
                storeService.deleteByUid(uid);
            }
            
            //删除用户数据可见范围
            userDataScopeService.deleteByUid(user.getUid());
        }
        return Pair.of(true, null);
    }

    @Override
    public Triple<Boolean, String, Object> updatePassword(PasswordQuery passwordQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("updatePassword  ERROR! not found user ");
            Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        User oldUser = queryByUserNameAndTenantId(passwordQuery.getName(), tenantId);
        if (Objects.isNull(oldUser)) {
            log.error("updatePassword  ERROR! not found userName{} ", passwordQuery.getName());
            Triple.of(false, null, "用户名不存在");
        }

        if (!Objects.equals(user, oldUser)) {
            log.error("updatePassword  ERROR! not found userId{},oldUserId{} ", user.getUid(), oldUser.getUid());
            Triple.of(false, null, "不能修改别人的密码");
        }

        User updateUser = new User();
        updateUser.setUid(oldUser.getUid());
        updateUser.setLoginPwd(customPasswordEncoder.encode(passwordQuery.getPassword()));
        updateUser.setUpdateTime(System.currentTimeMillis());
        Integer update = updateUser(updateUser, oldUser);

        return update > 0 ? Triple.of(true, null, null) : Triple.of(false, null, "修改密码失败!");
    }

    @Override
    public Integer updateUser(User updateUser, User oldUser) {
        Integer update = update(updateUser);
        if (update > 0) {
            redisService.delete(CacheConstant.CACHE_USER_UID + oldUser.getUid());
            redisService.delete(CacheConstant.CACHE_USER_PHONE + oldUser.getPhone() + ":" + oldUser.getUserType());
        }
        return update;
    }

    @Override
    public Pair<Boolean, Object> addUserAddress(String cityCode) {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            log.error("USER ERROR! no user!");
            return Pair.of(false, "未能查到用户信息!");
        }

        User user = queryByUidFromCache(uid);
        if (Objects.isNull(user)) {
            log.error("USER ERROR! no user! uid={}", uid);
            return Pair.of(false, "未能查到用户信息!");
        }

        City city = cityService.queryCityByCode(cityCode);
        if (Objects.isNull(city)) {
            log.error("USER ERROR! no city! cityCode={},uid={}", cityCode, uid);
            return Pair.of(false, "未能查到相关城市信息");
        }

        Province province = provinceService.queryByIdFromDB(city.getPid());
        if (Objects.isNull(province)) {
            log.error("USER ERROR! no province! provinceId={},uid={}", city.getPid(), uid);
            return Pair.of(false, "未能查到相关省份信息");
        }

        User updateUser = new User();
        updateUser.setUid(user.getUid());
        updateUser.setCid(city.getId());
        updateUser.setCity(city.getName());
        updateUser.setProvince(province.getName());
        updateUser.setUpdateTime(System.currentTimeMillis());
        updateUser(updateUser, user);

        return Pair.of(true, null);
    }

    @Override
    public Pair<Boolean, Object> getUserDetail() {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            log.error("USER ERROR! no use!");
            return Pair.of(false, "USER.0001");
        }

        User user = queryByUidFromCache(uid);
        if (Objects.isNull(user)) {
            log.error("USER ERROR! no found user! uid={}", uid);
            return Pair.of(false, "USER.0001");
        }

        HashMap<String, Object> resultMap = Maps.newHashMap();

        //处理用户信息
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        resultMap.put("user", userVo);

        return Pair.of(true, resultMap);
    }

    @Override
    public R endLimitUser(Long uid) {
        String orderLimit = redisService.get(CacheConstant.ORDER_TIME_UID + uid);
        if (Objects.isNull(orderLimit)) {
            return R.fail("ELECTRICITY.0062", "用户未被限制");
        }
        redisService.delete(CacheConstant.ORDER_TIME_UID + uid);
        return R.ok();
    }

    @Override
    public R addInnerUser(AdminUserQuery adminUserQuery) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        User phoneUserExists = queryByUserPhone(adminUserQuery.getPhone(), adminUserQuery.getUserType(), tenantId);
        if (Objects.nonNull(phoneUserExists)) {
            return R.fail("手机号已存在！");

        }

        User userNameExists = queryByUserName(adminUserQuery.getName());
        if (Objects.nonNull(userNameExists)) {
            return R.fail("用户名已经存在！");
        }

        //解密密码
        String encryptPassword = adminUserQuery.getPassword();
        String decryptPassword = decryptPassword(encryptPassword);
        if (StrUtil.isEmpty(decryptPassword)) {
            log.error("ADMIN USER ERROR! decryptPassword error! username={},phone={},password={}", adminUserQuery.getName(), adminUserQuery.getPhone(), adminUserQuery.getPassword());
            return R.fail("系统错误!");
        }

        //处理城市和省份
        City city = cityService.queryByIdFromDB(adminUserQuery.getCityId());
        Province province = provinceService.queryByIdFromDB(adminUserQuery.getProvinceId());

        User user = User.builder()
                .avatar("")
                .salt("")
                .createTime(System.currentTimeMillis())
                .delFlag(User.DEL_NORMAL)
                .lockFlag(User.USER_UN_LOCK)
                .gender(adminUserQuery.getGender())
                .lang(adminUserQuery.getLang())
                .loginPwd(customPasswordEncoder.encode(decryptPassword))
                .name(adminUserQuery.getName())
                .phone(adminUserQuery.getPhone())
                .updateTime(System.currentTimeMillis())
                .userType(adminUserQuery.getUserType())
                .salt("")
                .city(Objects.nonNull(city) ? city.getName() : null)
                .province(Objects.nonNull(province) ? province.getName() : null)
                .cid(Objects.nonNull(city) ? city.getId() : null)
                .tenantId(tenantId)
                .dataType(Objects.nonNull(adminUserQuery.getDataType()) ? adminUserQuery.getDataType() : null)
                .build();
        User insert = insert(user);

        //默认值
        Long roleId = adminUserQuery.getUserType().longValue() + 1;
        if (Objects.nonNull(adminUserQuery.getRoleId())) {
            roleId = adminUserQuery.getRoleId();
        }


        //运营商
        if (Objects.equals(adminUserQuery.getDataType(), User.DATA_TYPE_OPERATE)) {
            Long role = roleService.queryByName("OPERATE_USER", tenantId);
            if (Objects.nonNull(role)) {
                roleId = role;
            }

        }

        //加盟商
        if (Objects.equals(adminUserQuery.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            Long role = roleService.queryByName("FRANCHISEE_USER", tenantId);
            if (Objects.nonNull(role)) {
                roleId = role;
            }
        }

        //门店
        if (Objects.equals(adminUserQuery.getDataType(), User.DATA_TYPE_STORE)) {
            Long role = roleService.queryByName("STORE_USER", tenantId);
            if (Objects.nonNull(role)) {
                roleId = role;
            }
        }

        //设置角色
        UserRole userRole = new UserRole();
        userRole.setRoleId(roleId);
        userRole.setUid(insert.getUid());
        userRoleService.insert(userRole);

        if (Objects.nonNull(insert.getUid())) {
            return R.ok(insert.getUid());
        }

        return R.fail("系统错误!");
    }

    @Override
    public void deleteInnerUser(Long uid) {
        User user = queryByUidFromCache(uid);
        if (Objects.nonNull(user)) {
            if (deleteById(uid)) {
                redisService.delete(CacheConstant.CACHE_USER_UID + uid);
                redisService.delete(CacheConstant.CACHE_USER_PHONE + user.getPhone() + ":" + user.getUserType());
            }
        }
    }

    @Override
    public Pair<Boolean, Object> queryCount(Long uid, String name, String phone, Integer type, Long startTime, Long endTime, Integer tenantId) {
        return Pair.of(true, this.userMapper.queryCount(uid, name, phone, type, startTime, endTime, tenantId));
    }

    @Override
    public Integer queryHomePageCount(Integer type, Long startTime, Long endTime, Integer tenantId) {
        return this.userMapper.queryCount(null, null, null, type, startTime, endTime, tenantId);
    }

    @Override
    @Transactional
    public Triple<Boolean, String, Object> deleteNormalUser(Long uid) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        TokenUser userInfo = SecurityUtils.getUserInfo();
        if (Objects.isNull(userInfo)) {
            return Triple.of(false, "USER.0001", "登陆用户不合法，无法操作！");
        }

        if (!Objects.equals(User.TYPE_USER_SUPER, userInfo.getType()) && !Objects.equals(User.DATA_TYPE_OPERATE, userInfo.getDataType())) {
            return Triple.of(false, "AUTH.0002", "没有权限操作！");
        }

        User user = queryByUidFromCache(uid);
        if (Objects.isNull(user) || !user.getUserType().equals(User.TYPE_USER_NORMAL_WX_PRO)) {
            return Triple.of(false, "USER.0001", "没有此用户！无法删除");
        }

        if (!Objects.equals(tenantId, user.getTenantId())) {
            return Triple.of(true, null, null);
        }

        List<UserOauthBind> userOauthBinds = userOauthBindService.queryListByUid(uid);
        if (DataUtil.collectionIsUsable(userOauthBinds)) {
            delUserOauthBindAndClearToken(userOauthBinds);
        }
        //删除套餐
        UserInfo userInfo1 = userInfoService.queryByUidFromCache(uid);
        franchiseeUserInfoService.deleteByUserInfoId(userInfo1.getId());
        //删除用户
        deleteWxProUser(uid, userInfo1.getTenantId());
        userInfoService.deleteByUid(uid);

        return Triple.of(true, null, null);
    }

    @Override
    public R userAutoCodeGeneration() {
        if (!Objects.equals(SecurityUtils.getUserInfo().getType(), User.TYPE_USER_SUPER)) {
            return R.fail("权限不足");
        }

        String key = RandomUtil.randomString(6);
        redisService.set(key, key);
        redisService.expire(key, TimeUnit.MINUTES.toMillis(10), false);

        return R.ok(key);
    }

    @Override
    public R userAutoCodeCheck(String autoCode) {
        String key = redisService.get(autoCode);
        if (StringUtils.isBlank(key)) {
            return R.fail("验证码已使用或不存在");
        }

        redisService.delete(autoCode);

        return R.ok();
    }

    private void delUserOauthBindAndClearToken(List<UserOauthBind> userOauthBinds) {
        userOauthBinds.parallelStream().forEach(e -> {
            String thirdId = e.getThirdId();
            List<String> tokens = redisService.getWithList(TokenConstant.CACHE_LOGIN_TOKEN_LIST_KEY + CacheConstant.CLIENT_ID + e.getTenantId() + ":" + thirdId, String.class);
            if (DataUtil.collectionIsUsable(tokens)) {
                tokens.stream().forEach(s -> {
                    redisService.delete(TokenConstant.CACHE_LOGIN_TOKEN_KEY + CacheConstant.CLIENT_ID + s);
                });
            }
            userOauthBindService.deleteById(e.getId());
        });

    }

    private void deleteWxProUser(Long uid, Integer tenantId) {
        User user = queryByUidFromCache(uid);
        if (Objects.nonNull(user)) {
            if (deleteById(uid)) {
                redisService.delete(CacheConstant.CACHE_USER_UID + uid);
                redisService.delete(CacheConstant.CACHE_USER_PHONE + tenantId + user.getPhone() + ":" + user.getUserType());
            }
        }
    }

}
