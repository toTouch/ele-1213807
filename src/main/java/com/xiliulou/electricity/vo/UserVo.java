package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author: eclair
 * @Date: 2021/1/22 08:25
 * @Description:
 */
@Data
public class UserVo {
	/**
	 * 手机号
	 */
	private String phone;

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
	/**
	 * 城市
	 */
	private String city;
	/**
	 * 省份
	 */
	private String province;
	/**
	 * 详细地址
	 */
	private String address;
	/**
	 * 城市id
	 */
	private Integer cid;
	/**
	 * 推荐人id
	 */
	private Long refId;
}
