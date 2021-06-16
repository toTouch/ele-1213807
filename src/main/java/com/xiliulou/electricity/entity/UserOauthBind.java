package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (UserOauthBind)实体类
 *
 * @author Eclair
 * @since 2020-12-03 09:17:39
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_oauth_bind")
public class UserOauthBind {

	@TableId
	private Long id;

	private Long uid;
	/**
	 * 1-wx公证号 2--wx小程序 3--支付宝xx
	 */
	private Integer source;
	/**
	 * 第三方id
	 */
	private String thirdId;
	/**
	 * 第三方的nickName
	 */
	private String thirdNick;
	/**
	 * 第三方的访问token！
	 */
	private String accessToken;
	/**
	 * 第三方的刷新token！
	 */
	private String refreshToken;
	/**
	 * 1--绑定，2--解绑
	 */
	private Integer status;

	private Long createTime;

	private Long updateTime;
	/**
	 * 用户手机号
	 */
	private String phone;

	private Integer tenantId;
	//绑定
	public static final Integer STATUS_BIND = 1;
	//解绑
	public static final Integer STATUS_UN_BIND = 2;

	//公众号
	public static final Integer SOURCE_WX = 1;
	//小程序
	public static final Integer SOURCE_WX_PRO = 2;

}
