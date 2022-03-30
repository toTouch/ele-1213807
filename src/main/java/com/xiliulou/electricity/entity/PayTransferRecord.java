package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Miss.Li
 * @Date: 2021/8/30 09:33
 * @Description:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_pay_transfer_record")
public class PayTransferRecord {

	//初始化
	public static final Integer STATUS_INIT = 0;
	//转账中
	public static final Integer STATUS_TRANSFER_ING = 1;
	//转账成功
	public static final Integer STATUS_OK = 2;
	//转账失败
	public static final Integer STATUS_FAILED = -1;


	private Long id;
	//内部订单号
	private String orderId;
	//渠道订单号(微信内部订单号,方便查询)
	private String channelOrderNo;
	//商户号
	private String channelMchId;
	private String channelMchAppId;
	//错误码
	private String errorCode;
	//错误描述
	private String errorMsg;
	//创建时间
	private Long createTime;
	//修改时间
	private Long updateTime;
	//交易号
	private String transactionOrderNo;
	//用户id
	private Long uid;
	//实际到账金额
	private Long actualAmount;
	//手续费
	private Long commissionAmount;
	//请求转账金额(包含手续费)
	private Long requestAmount;
	//收款人银行卡号
	private String encBankNo;
	//收款人真实姓名
	private String encTrueName;
	//银行编号
	private String bankNo;
	//转账描述
	private String description;
	//转账状态
	private Integer status;
	//租户id
	private Integer tenantId;


	public static final String TRANSFER_RESULT_FAIL = "FAIL";

	public static final String TRANSFER_RESULT_SUCCESS = "SUCCESS";

	public static final Integer ERROR_TYPE_SAAS = 1;
	public static final Integer ERROR_TYPE_WEIXIN = 2;

	public static final String TRANSFER_RESULT_STATUS_SUCCESS = "SUCCESS";
	public static final String TRANSFER_RESULT_STATUS_FAIL = "FAILED";
	public static final String TRANSFER_RESULT_STATUS_PROCESSING = "PROCESSING";
}
