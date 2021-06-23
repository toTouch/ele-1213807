package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.config.RolePermissionConfig;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.PermissionResource;
import com.xiliulou.electricity.entity.Role;
import com.xiliulou.electricity.entity.RolePermission;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserRole;
import com.xiliulou.electricity.mapper.TenantMapper;
import com.xiliulou.electricity.query.TenantQuery;
import com.xiliulou.electricity.service.RolePermissionService;
import com.xiliulou.electricity.service.RoleService;
import com.xiliulou.electricity.service.UserRoleService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.security.authentication.console.CustomPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
/**
 * 租户表(Tenant)表服务实现类
 *
 * @author makejava
 * @since 2021-06-16 14:31:45
 */
@Service("tenantService")
@Slf4j
public class TenantServiceImpl implements TenantService {

    @Value("${security.encode.key:xiliu&lo@u%12345}")
    private String encodeKey;

    @Resource
    private TenantMapper tenantMapper;

    @Resource
    private UserService userService;

    @Autowired
    private CustomPasswordEncoder customPasswordEncoder;

    @Autowired
    RoleService roleService;

    @Autowired
    UserRoleService userRoleService;

    @Autowired
    private RolePermissionConfig permissionConfig;

    @Autowired
    private RolePermissionService rolePermissionService;



    /**
     * 新增数据
     *
     * @param tenantQuery 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R addTenantId(TenantQuery tenantQuery) {

        //判断用户名是否存在
        if (!Objects.isNull(userService.queryByUserName(tenantQuery.getName()))) {
            return R.fail("LOCKER.10015", "用户名已存在");
        }

        //1.保存租户信息
        tenantQuery.setCode(genTenantCode());
        tenantQuery.setCreateTime(System.currentTimeMillis());
        tenantQuery.setDelFlag(Tenant.DEL_NORMAL);
        tenantQuery.setStatus(Tenant.STA_NO_OUT);
        tenantQuery.setUpdateTime(System.currentTimeMillis());

        Tenant tenant = new Tenant();
        BeanUtil.copyProperties(tenantQuery, tenant);
        tenant.setExpireTime(System.currentTimeMillis() + 7 * 24 * 3600 * 1000);
        tenantMapper.insert(tenant);

        //2.保存用户信息
        String decryptPassword = decryptPassword(tenantQuery.getPassword());
        if (StrUtil.isEmpty(decryptPassword)) {
            log.error("ADMIN USER ERROR! decryptPassword error! username={},phone={},password={}", tenantQuery.getName(), tenantQuery.getPhone(), tenantQuery.getPassword());
            return R.fail("SYSTEM.0001", "系统错误!");
        }
        String loginPwd = StrUtil.isEmpty(decryptPassword) ? null : customPasswordEncoder.encode(decryptPassword);

        User user = new User();
        user.setName(tenantQuery.getName());
        user.setLoginPwd(loginPwd);
        user.setPhone(tenantQuery.getPhone());
        user.setAvatar("");
        user.setGender(User.GENDER_MALE);
        user.setDelFlag(User.DEL_NORMAL);
        user.setLockFlag(User.USER_UN_LOCK);
        user.setUserType(User.TYPE_USER_FRANCHISEE);
        user.setCreateTime(System.currentTimeMillis());
        user.setUpdateTime(System.currentTimeMillis());
        user.setLang(User.DEFAULT_LANG);
        user.setCity("");
        user.setProvince("");
        userService.insert(user);
        if (user.getUid() == null) {
            return R.fail("ELECTRICITY.0086", "操作失败");
        }

        //3.构建三大角色，运营商，代理商，门店
        Role operateRole = new Role();
        operateRole.setName(ElectricityCabinetConstant.OPERATE_NAME);
        operateRole.setCode(ElectricityCabinetConstant.OPERATE_CODE);

        Role franchiseeRole = new Role();
        franchiseeRole.setName(ElectricityCabinetConstant.FRANCHISEE_NAME);
        franchiseeRole.setCode(ElectricityCabinetConstant.FRANCHISEE_CODE);

        Role storeRole = new Role();
        storeRole.setName(ElectricityCabinetConstant.STORE_NAME);
        storeRole.setCode(ElectricityCabinetConstant.STORE_CODE);

        ArrayList<Role> roleList = new ArrayList<>();
        roleList.add(operateRole);
        roleList.add(franchiseeRole);
        roleList.add(storeRole);

        roleList.forEach(item -> {
            item.setTenantId(tenant.getId());
            item.setUpdateTime(System.currentTimeMillis());
            item.setCreateTime(System.currentTimeMillis());
            item.setDesc("tenantRole");
            roleService.insert(item);
        });

        // 4.构建角色用户关联表信息
        UserRole userRole = new UserRole();
        userRole.setUid(user.getUid());
        userRole.setRoleId(operateRole.getId());
        userRoleService.insert(userRole);
        if (Objects.isNull(userRole)) {
            return R.fail("ELECTRICITY.0086", "操作失败");
        }

        //5.角色赋予权限
        ArrayList<RolePermission> rolePermissionList = new ArrayList<>();
        permissionConfig.getOperator().forEach(item -> {
            RolePermission operatorRP = new RolePermission();
            operatorRP.setPId(item);
            operatorRP.setRoleId(operateRole.getId());
            rolePermissionList.add(operatorRP);
        });
        permissionConfig.getAlliance().forEach(item -> {
            RolePermission allianceRP = new RolePermission();
            allianceRP.setPId(item);
            allianceRP.setRoleId(franchiseeRole.getId());
            rolePermissionList.add(allianceRP);
        });
        permissionConfig.getShop().forEach(item -> {
            RolePermission shopRP = new RolePermission();
            shopRP.setPId(item);
            shopRP.setRoleId(storeRole.getId());
        });
        rolePermissionList.parallelStream().forEach(e -> {

            rolePermissionService.insert(e);
        });

        return R.ok();
    }

    /**
     * 修改数据
     *
     * @param tenant 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(Tenant tenant) {
       return tenantMapper.updateById(tenant);

    }

    /**
     * 生成新的租户code
     */
    private String genTenantCode() {
        String code = RandomUtil.randomNumbers(6);
        if (tenantMapper.selectCount(Wrappers.<Tenant>lambdaQuery().eq(Tenant::getCode, code)) == 0) {
            return code;
        }
        return genTenantCode();
    }

    private String decryptPassword(String encryptPassword) {
        AES aes = new AES(Mode.CBC, Padding.ZeroPadding, new SecretKeySpec(encodeKey.getBytes(), "AES"),
                new IvParameterSpec(encodeKey.getBytes()));

        return new String(aes.decrypt(Base64.decode(encryptPassword.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
    }

}
