package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.InvitationActivityUser;
import com.xiliulou.electricity.query.InvitationActivityUserQuery;
import com.xiliulou.electricity.vo.InvitationActivityUserVO;
import com.xiliulou.electricity.vo.UserInfoVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * (InvitationActivityUser)表服务接口
 *
 * @author zzlong
 * @since 2023-06-05 16:11:08
 */
public interface InvitationActivityUserService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    InvitationActivityUser queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    InvitationActivityUser queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<InvitationActivityUser> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param invitationActivityUser 实例对象
     * @return 实例对象
     */
    InvitationActivityUser insert(InvitationActivityUser invitationActivityUser);

    /**
     * 修改数据
     *
     * @param invitationActivityUser 实例对象
     * @return 实例对象
     */
    Integer update(InvitationActivityUser invitationActivityUser);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    List<InvitationActivityUserVO> selectByPage(InvitationActivityUserQuery query);

    Integer selectByPageCount(InvitationActivityUserQuery query);

    Triple<Boolean, String, Object> save(Long uid);

    Triple<Boolean, String, Object> delete(Long id);

    InvitationActivityUser selectByUid(Long uid);
}
