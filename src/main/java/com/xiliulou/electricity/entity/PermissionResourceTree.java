package com.xiliulou.electricity.entity;

import lombok.Data;

/**
 * @author: eclair
 * @Date: 2020/12/14 11:15
 * @Description:
 */
@Data
public class PermissionResourceTree extends TreeNode {
	private String name;
	private Integer type;
	private String url;
	private String method;
	private Double sort;
	private String desc;
	private Long createTime;

}
