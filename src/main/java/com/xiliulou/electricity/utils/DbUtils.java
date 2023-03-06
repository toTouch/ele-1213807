package com.xiliulou.electricity.utils;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author: eclair
 * @Date: 2020/12/1 10:16
 * @Description:
 */
public class DbUtils {
    
    @Deprecated
    public static <T> T dbOperateSuccessThen(Integer id, Supplier<T> supplier) {
        if (id > 0) {
            return supplier.get();
        }
        return null;
    }
    
    /**
     * 当数据更新成功后，直接删除缓存
     * @param id
     * @param consumer
     * @return
     */
    public static Integer dbOperateSuccessThenHandleCache(Integer id, Consumer<Integer> consumer) {
        if (id > 0) {
            consumer.accept(id);
            return id;
        }
        return id;
    }
}
