package com.xiliulou.electricity.controller.error;

import com.alibaba.druid.wall.violation.ErrorCode;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: eclair
 * @Date: 2020/11/27 08:26
 * @Description:
 */
@RestController
public class CustomErrorController implements ErrorController {
	private static final String ERROR_PATH = "/error";
	private ErrorAttributes errorAttributes;

	@Override
	public String getErrorPath() {
		return ERROR_PATH;
	}

	@Autowired
	public CustomErrorController(ErrorAttributes errorAttributes) {
		this.errorAttributes = errorAttributes;
	}

	/**
	 * web页面错误处理
	 */
	@RequestMapping(value = ERROR_PATH, produces = "text/html")
	@ResponseBody
	public String errorPageHandler(HttpServletRequest request, HttpServletResponse response) {
		ServletWebRequest requestAttributes = new ServletWebRequest(request);
		Map<String, Object> attr = this.errorAttributes.getErrorAttributes(requestAttributes, false);

		HashMap<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("path", attr.get("path"));
		resultMap.put("message", attr.get("message"));
		resultMap.put("status", attr.get("status"));
		return JsonUtil.toJson(R.fail(resultMap, "SYSTEM.0001", (String) attr.get("message")));
	}

	@RequestMapping(value = ERROR_PATH)
	@ResponseBody
	public R errorApiHandler(HttpServletRequest request) {
		ServletWebRequest requestAttributes = new ServletWebRequest(request);
		Map<String, Object> attr = this.errorAttributes.getErrorAttributes(requestAttributes, false);

		HashMap<String, Object> resultMap = Maps.newHashMap();
		resultMap.put("path", attr.get("path"));
		resultMap.put("message", attr.get("message"));
		resultMap.put("status", attr.get("status"));

		return R.fail(resultMap, "SYSTEM.0001", (String) attr.get("message"));
	}

}
