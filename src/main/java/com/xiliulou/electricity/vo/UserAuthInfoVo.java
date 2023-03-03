package com.xiliulou.electricity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author: eclair
 * @Date: 2021/4/2 09:04
 * @Description:
 */
@Data
public class UserAuthInfoVo {
	private Long id;

	private Long uid;
	/**
	 * 手机号
	 */
	private String phone;
	/**
	 * 用户名
	 */
	private String userName;
	/**
	 * 用户姓名
	 */
	private String name;
	/**
	 * 邮箱
	 */
	private String mailbox;
	/**
	 * 身份证号
	 */
	private String idNumber;
	//审核状态(0--等待审核中,1--审核被拒绝,2--审核通过,3--活体检测失败,4--活体检测成功)
	private Integer authStatus;
	/**
	 * 身份证认证正面
	 */
	private String idCardFrontUrl;

	/**
	 * 身份证认证反面
	 */
	private String idCardBackUrl;

	/**
	 * 自拍照片
	 */
	private String selfPhoto;

	/**
	 * 认证时间
	 */
	private Long createTime;

	/**
	 * 初始电池编号
	 */
	private String initElectricityBatterySn;
	/**
	 * 当前电池编号
	 */
	private String nowElectricityBatterySn;
	/**
	 * 服务状态 (0--初始化,1--已实名认证，2--已缴纳押金，3--已租电池)
	 */
	private Integer serviceStatus;

	/**
	 * 可用状态(0--启用，1--禁用)
	 */
	private Integer usableStatus;

	/**
	 * 实名认证审核类型，1：人工审核，2：自动审核，3：人脸审核
	 */
	private Integer authType;

}
