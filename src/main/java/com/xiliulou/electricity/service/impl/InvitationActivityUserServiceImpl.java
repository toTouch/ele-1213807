package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.InvitationActivityUser;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.InvitationActivityUserMapper;
import com.xiliulou.electricity.query.InvitationActivityUserQuery;
import com.xiliulou.electricity.service.InvitationActivityUserService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.InvitationActivityUserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * (InvitationActivityUser)表服务实现类
 *
 * @author zzlong
 * @since 2023-06-05 16:11:08
 */
@Service("invitationActivityUserService")
@Slf4j
public class InvitationActivityUserServiceImpl implements InvitationActivityUserService {
    @Resource
    private InvitationActivityUserMapper invitationActivityUserMapper;
    @Autowired
    private UserInfoService userInfoService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public InvitationActivityUser queryByIdFromDB(Long id) {
        return this.invitationActivityUserMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public InvitationActivityUser queryByIdFromCache(Long id) {
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
    public List<InvitationActivityUser> queryAllByLimit(int offset, int limit) {
        return this.invitationActivityUserMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param invitationActivityUser 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvitationActivityUser insert(InvitationActivityUser invitationActivityUser) {
        this.invitationActivityUserMapper.insertOne(invitationActivityUser);
        return invitationActivityUser;
    }

    /**
     * 修改数据
     *
     * @param invitationActivityUser 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(InvitationActivityUser invitationActivityUser) {
        return this.invitationActivityUserMapper.update(invitationActivityUser);

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
        return this.invitationActivityUserMapper.deleteById(id) > 0;
    }

    @Override
    public List<InvitationActivityUserVO> selectByPage(InvitationActivityUserQuery query) {
        return invitationActivityUserMapper.selectByPage(query);
    }

    @Override
    public Integer selectByPageCount(InvitationActivityUserQuery query) {
        return invitationActivityUserMapper.selectByPageCount(query);
    }

    @Override
    public Triple<Boolean, String, Object> save(InvitationActivityUserQuery query) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        InvitationActivityUser invitationActivityUser1 = this.selectByUid(query.getUid());
        if(Objects.nonNull(invitationActivityUser1)){
            return Triple.of(false, "", "用户已存在");
        }

        InvitationActivityUser invitationActivityUser = new InvitationActivityUser();
        invitationActivityUser.setUid(query.getUid());
        invitationActivityUser.setActivityId(query.getActivityId());
        invitationActivityUser.setOperator(SecurityUtils.getUid());
        invitationActivityUser.setCreateTime(System.currentTimeMillis());
        invitationActivityUser.setUpdateTime(System.currentTimeMillis());
        invitationActivityUser.setTenantId(TenantContextHolder.getTenantId());

        this.invitationActivityUserMapper.insertOne(invitationActivityUser);

        return Triple.of(true, null, null);
    }

    @Override
    public Triple<Boolean, String, Object> delete(Long id) {
        InvitationActivityUser invitationActivityUser = this.queryByIdFromDB(id);
        if (Objects.isNull(invitationActivityUser) || !Objects.equals(invitationActivityUser.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        invitationActivityUserMapper.deleteById(id);

        return Triple.of(true, null, null);
    }

    @Override
    public InvitationActivityUser selectByUid(Long uid) {
        return this.invitationActivityUserMapper.selectByUid(uid);
    }
}
