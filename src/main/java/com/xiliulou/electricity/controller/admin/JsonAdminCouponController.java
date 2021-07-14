package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.client.identify.Base64;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.CouponQuery;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.pay.weixin.entity.AccessTokenResult;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

/**
 * 优惠券规则表(TCoupon)表控制层
 *
 * @author makejava
 * @since 2021-04-14 09:28:22
 */
@RestController
@Slf4j
public class JsonAdminCouponController {
	/**
	 * 服务对象
	 */
	@Autowired
	private CouponService couponService;

	@Autowired
	FranchiseeService franchiseeService;

	//新增
	@PostMapping(value = "/admin/coupon")
	public R save(@RequestBody @Validated(value = CreateGroup.class) Coupon coupon) {
		return couponService.insert(coupon);
	}

	//修改--暂时无此功能
	@PutMapping(value = "/admin/coupon")
	public R update(@RequestBody @Validated(value = UpdateGroup.class) Coupon coupon) {
		return couponService.update(coupon);
	}

	//删除--暂时无此功能
	@DeleteMapping(value = "/admin/coupon/{id}")
	public R delete(@PathVariable("id") Integer id) {
		if (Objects.isNull(id)) {
			return R.fail("ELECTRICITY.0007", "不合法的参数");
		}
		return couponService.delete(id);
	}

	//列表查询
	@GetMapping(value = "/admin/coupon/list")
	public R queryList(@RequestParam(value = "size", required = false) Long size,
			@RequestParam(value = "offset", required = false) Long offset,
			@RequestParam(value = "discountType", required = false) Integer discountType,
			@RequestParam(value = "franchiseeId", required = false) Integer franchiseeId,
			@RequestParam(value = "name", required = false) String name) {
		if (Objects.isNull(size)) {
			size = 10L;
		}

		if (Objects.isNull(offset) || offset < 0) {
			offset = 0L;
		}

		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		if (Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {
			Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
			if (Objects.isNull(franchisee)) {
				return R.ok();
			}
			franchiseeId = franchisee.getId();
		}

		CouponQuery couponQuery = CouponQuery.builder()
				.offset(offset)
				.size(size)
				.name(name)
				.discountType(discountType)
				.franchiseeId(franchiseeId).build();
		return couponService.queryList(couponQuery);
	}

	//测试
	@GetMapping(value = "/outer/test")
	public R test() {
		//获取缓存accesstoken
		String accessToken =
				getAccessToken("wx76159ea6aa7a64bc", "b44586ca1b4ff8def2b4c869cdd8ea6a");
		if (ObjectUtil.isEmpty(accessToken)) {
			return R.fail("失败");
		}


		String url = " https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + accessToken;

		//发送给微信服务器的数据
		String scene="1";

		//发送给微信服务器的数据
		String jsonStr = "{\"scene\": " + scene + ",\"page\": \"pages/start/index\"}";


		//post请求得到返回数据（这里是封装过的，就是普通的java post请求）
		String response = sendPost(jsonStr, url);
		return R.ok(response);

	}

	private String getAccessToken(String appId, String appSecret) {

		String getAccessToken = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appId + "&secret=" + appSecret;
		String result = HttpUtil.get(getAccessToken);
		String accessToken = JSONUtil.toBean(result, AccessTokenResult.class).getAccess_token();
		log.info("GET ACCESS_TOKEN  RESULT：" + result);
		return accessToken;
	}

	//post请求
	public static String sendPost(String param, String url) {
		PrintWriter out = null;
		InputStream in = null;
		String result = "";
		try {

			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			// out = new PrintWriter(conn.getOutputStream());
			out = new PrintWriter(new OutputStreamWriter(
					conn.getOutputStream(), "utf-8"));
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			out = new PrintWriter(conn.getOutputStream());
			in = conn.getInputStream();
			byte[] data = null;
			// 读取图片字节数组
			try {
				ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
				byte[] buff = new byte[100];
				int rc = 0;
				while ((rc = in.read(buff, 0, 100)) > 0) {
					swapStream.write(buff, 0, rc);
				}
				data = swapStream.toByteArray();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return new String(data);
		} catch (Exception e) {
			log.info("发送 POST 请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

}
