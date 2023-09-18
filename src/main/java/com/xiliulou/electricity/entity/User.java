package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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

	@TableId(value = "uid", type = IdType.AUTO)
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
	 * 0--超级管理员 1--运营商  11--微信小程序用户
	 */
	private Integer userType;
	/**
	 * 数据类型 ：0--超级管理员 1--运营商 2--加盟商 3--门店
	 */
	private Integer dataType;
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

	/**
	 * 用户来源，1：扫码，2：邀请，3：其它
	 */
	private Integer source;

	private Integer tenantId;

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


	/**
	 * 用户类型  0：超级管理员，1:普通管理员  11：普通微信小程序用户
	 */
	public static final Integer TYPE_USER_SUPER = 0;
	@Deprecated
	public static final Integer TYPE_USER_OPERATE = 1;
	@Deprecated
	public static final Integer TYPE_USER_FRANCHISEE =2;
	@Deprecated
	public static final Integer TYPE_USER_STORE=3;
	
	public static final Integer TYPE_USER_NORMAL_WX_PRO = 11;
	
	public static final Integer TYPE_USER_NORMAL_ADMIN = 1;


	/**
	 * 用户数据可见范围   1：运营商，2：加盟商，3：门店
	 */
	public static final Integer DATA_TYPE_OPERATE = 1;
	public static final Integer DATA_TYPE_FRANCHISEE =2;
	public static final Integer DATA_TYPE_STORE=3;


	//用户来源，1：扫码，2：邀请，3：其它
	@Deprecated
	public static final Integer SOURCE_TYPE_SCAN=1;
	@Deprecated
	public static final Integer SOURCE_TYPE_INVITE=2;
	@Deprecated
	public static final Integer SOURCE_TYPE_ONLINE=3;


	//默认语言
	public static final String DEFAULT_LANG = "zh-CN";

}
