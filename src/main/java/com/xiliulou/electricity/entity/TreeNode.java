package com.xiliulou.electricity.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: eclair
 * @Date: 2020/12/14 11:14
 * @Description:
 */
@Data
public class TreeNode {
	//当前节点
	protected  long id;
	//父节点
	protected  long parentId;

	protected List<TreeNode> children = new ArrayList<TreeNode>();

	public void add(TreeNode node) {
		children.add(node);
	}
}
