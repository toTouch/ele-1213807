package com.xiliulou.electricity.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName: BaseDataUtil
 * @description:
 * @author: renhang
 * @create: 2024-12-02 15:53
 */
@SuppressWarnings("all")
public class BaseDataUtil {
    
    
    /**
     * 组装集合中的所有二元组合
     *
     * @param set 输入集合
     * @return 集合中的所有二元组合
     */
    public static void buildCombinations(Set<Long> set, Map<String, Set<Long>> combinations) {
        Long[] elements = set.toArray(new Long[0]);
        
        for (int i = 0; i < elements.length; i++) {
            for (int j = i + 1; j < elements.length; j++) {
                // 对组合元素排序，确保 (a, b) 和 (b, a) 被视为相同的组合
                Set<Long> pair = new HashSet<>();
                pair.add(elements[i]);
                pair.add(elements[j]);
                List<Long> sortedList = new ArrayList<>(pair);
                Collections.sort(sortedList);
                String key = sortedList.toString();
                combinations.put(key, pair);
            }
        }
    }
    
    /**
     * 判断是否可以添加集合2的组合
     *
     * @param set2                 要检查的集合2
     * @param existingCombinations 集合1中已有的二元组合
     * @return true 如果没有冲突，可以添加；false 如果有冲突，不能添加
     */
    public static boolean canAddCombination(Set<Long> set2, Map<String, Set<Long>> existingCombinations) {
        Long[] elements = set2.toArray(new Long[0]);
        // 检查集合2中的所有二元组合是否与集合1中已有的组合冲突
        for (int i = 0; i < elements.length; i++) {
            for (int j = i + 1; j < elements.length; j++) {
                Set<Long> pair = new HashSet<>();
                pair.add(elements[i]);
                pair.add(elements[j]);
                List<Long> sortedList = new ArrayList<>(pair);
                Collections.sort(sortedList);
                String key = sortedList.toString();
                
                // 如果组合已存在，则返回 false
                if (existingCombinations.containsKey(key)) {
                    return false;
                }
            }
        }
        return true;
    }
}
