package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.FranchiseeBindCard;
import com.xiliulou.electricity.query.FranchiseeBindCardBindQuery;

import java.util.List;

/**
 * 加盟商套餐绑定表(TFranchiseeBindCard)表服务接口
 *
 * @author makejava
 * @since 2021-04-16 15:12:51
 */
public interface FranchiseeBindCardService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    FranchiseeBindCard queryByIdFromDB(Long id);



    /**
     * 新增数据
     *
     * @param franchiseeBindCard 实例对象
     * @return 实例对象
     */
    FranchiseeBindCard insert(FranchiseeBindCard franchiseeBindCard);

    /**
     * 修改数据
     *
     * @param franchiseeBindCard 实例对象
     * @return 实例对象
     */
    Integer update(FranchiseeBindCard franchiseeBindCard);


    R bindCard(FranchiseeBindCardBindQuery franchiseeBindCardBindQuery);

    R queryBindCard(Integer id);

	List<FranchiseeBindCard> queryByFranchisee(Integer id);
}
