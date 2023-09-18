package com.xiliulou.electricity.service.enterprise;

import com.xiliulou.electricity.entity.enterprise.EnterpriseCloudBeanOrder;
import com.xiliulou.electricity.query.enterprise.EnterpriseCloudBeanOrderQuery;
import com.xiliulou.electricity.vo.enterprise.EnterpriseCloudBeanOrderVO;

import java.util.List;

/**
 * 企业云豆充值订单表(EnterpriseCloudBeanOrder)表服务接口
 *
 * @author zzlong
 * @since 2023-09-15 09:29:15
 */
public interface EnterpriseCloudBeanOrderService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    EnterpriseCloudBeanOrder queryByIdFromDB(Long id);

    /**
     * 修改数据
     *
     * @param enterpriseCloudBeanOrder 实例对象
     * @return 实例对象
     */
    Integer update(EnterpriseCloudBeanOrder enterpriseCloudBeanOrder);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    List<EnterpriseCloudBeanOrderVO> selectByPage(EnterpriseCloudBeanOrderQuery query);

    Integer selectByPageCount(EnterpriseCloudBeanOrderQuery query);

    Integer insert(EnterpriseCloudBeanOrder enterpriseCloudBeanOrder);
}
