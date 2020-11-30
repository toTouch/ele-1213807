package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.User;

import java.util.List;

/**
 * (User)表服务接口
 *
 * @author makejava
 * @since 2020-11-27 11:19:51
 */
public interface UserService {

	/**
	 * 通过ID查询单条数据从数据库
	 *
	 * @param uid 主键
	 * @return 实例对象
	 */
	User queryByIdFromDB(Long uid);

	/**
	 * 通过ID查询单条数据从缓存
	 *
	 * @param uid 主键
	 * @return 实例对象
	 */
	User queryByIdFromCache(Long uid);

	/**
	 * 查询多条数据
	 *
	 * @param offset 查询起始位置
	 * @param limit  查询条数
	 * @return 对象列表
	 */
	List<User> queryAllByLimit(int offset, int limit);

	/**
	 * 新增数据
	 *
	 * @param user 实例对象
	 * @return 实例对象
	 */
	User insert(User user);

	/**
	 * 修改数据
	 *
	 * @param user 实例对象
	 * @return 实例对象
	 */
	Integer update(User user);

	/**
	 * 通过主键删除数据
	 *
	 * @param uid 主键
	 * @return 是否成功
	 */
	Boolean deleteById(Long uid);

	User queryByUserName(String username);

}