package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author: Miss.Li
 * @Date: 2021/8/13 14:13
 * @Description:
 */
@Data
public class callerInfoVO {
	private String classLoaderName;
	private String moduleName;
	private String moduleVersion;
	private String methodName;
	private String fileName;
	private Integer lineNumber;
	private String className;
	private Boolean nativeMethod;
}
