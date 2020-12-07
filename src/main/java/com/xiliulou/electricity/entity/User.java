package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (User)实体类
 *
 * @author Eclair
 * @since 2020-11-27 11:19:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user")
public class User {

	@TableId
	private Long uid;
	/**
	 * 手机号
	 */
	private String phone;
	/**
	 * 密码
	 */
	private String loginPwd;
	/**
	 * 随机盐
	 */
	private String salt;
	/**
	 * 头像
	 */
	private String avatar;
	/**
	 * 用户姓名
	 */
	private String name;
	/**
	 * 性别 男-0 女-1
	 */
	private Integer gender;
	/**
	 * 0--正常 1--删除
	 */
	private Integer delFlag;
	/**
	 * 0--正常 1--锁住
	 */
	private Integer lockFlag;
	/**
	 * 0--超级管理员 1--运营商 2--用户
	 */
	private Integer userType;
	/**
	 * 创建时间
	 */
	private Long createTime;
	/**
	 * 更新时间
	 */
	private Long updateTime;
	/**
	 * 语言和国家
	 */
	private String lang;

	public boolean isLock() {
		return this.lockFlag.equals(USER_LOCK);
	}

	public static final Integer DEL_NORMAL = 0;
	public static final Integer DEL_DEL = 1;

	public static final Integer GENDER_MALE = 0;
	public static final Integer GENDER_FEMALE = 1;

	//锁住用户
	public static final Integer USER_LOCK = 1;
	//没有锁住用户
	public static final Integer USER_UN_LOCK = 0;

	//超级管理员
	public static final Integer TYPE_USER_SUPER = 0;
	//运营商
	public static final Integer TYPE_USER_OPERATE = 1;
	//普通用户
	public static final Integer TYPE_USER_NORMAL = 11;

}