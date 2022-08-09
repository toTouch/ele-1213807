package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UpgradeNotifyMail;
import com.xiliulou.electricity.query.UpgradeNotifyMailQuery;
import com.xiliulou.electricity.vo.UpgradeNotifyMailVO;

import java.util.List;

/**
 * (UpgradeNotifyMail)表服务接口
 *
 * @author zzlong
 * @since 2022-08-08 15:30:14
 */
public interface UpgradeNotifyMailService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    UpgradeNotifyMail selectByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    UpgradeNotifyMail selectByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<UpgradeNotifyMail> selectByPage(int offset, int limit);

    /**
     * 新增数据
     *
     * @param
     * @return 实例对象
     */
    R insert(UpgradeNotifyMailQuery upgradeNotifyMailQuery);

    /**
     * 修改数据
     *
     * @param upgradeNotifyMail 实例对象
     * @return 实例对象
     */
    Integer update(UpgradeNotifyMail upgradeNotifyMail);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    List<UpgradeNotifyMail> selectByTenantId();

    R insertOrUpdate(UpgradeNotifyMailQuery upgradeNotifyMailQuery);

    Boolean checkByTenantId();

}
