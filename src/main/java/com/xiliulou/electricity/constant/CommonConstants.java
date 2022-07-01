package com.xiliulou.electricity.constant;

/**
 * @author: eclair
 * @Date: 2021/3/22 10:33
 * @Description:
 */
public interface CommonConstants {
	String TENANT_ID = "tenantId";
	Integer TENANT_ID_DEFAULT = 0;

	/**
	 * 验证码前缀
	 */
	String DEFAULT_CODE_KEY = "DEFAULT_CODE_KEY_";

	//操作银行卡 用户锁
	String BIND_BANK_OPER_USER_LOCK = "bind_Bank_oper_user_lock:";


	//提现密码缓存
	String CACHE_WITHDRAW_PASSWORD = "withdraw_password";

	//提现 用户锁
	String CACHE_WITHDRAW_USER_UID = "withdraw_user_uid:";


	/**
	 * 柜机状态
	 */
	//在线
	String STATUS_ONLINE="online";
	//离线
	String STATUS_OFFLINE="offline";


}
