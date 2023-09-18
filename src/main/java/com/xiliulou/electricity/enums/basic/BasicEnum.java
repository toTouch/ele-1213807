package com.xiliulou.electricity.enums.basic;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 基础 Enum 接口
 * @author: xiaohui.song
 **/
public interface BasicEnum<K, V> {

    K getCode();

    V getDesc();

    /**
     * 校验枚举编码是否存在
     * @param code
     * @param clazz
     * @return
     */
    static boolean isExist(Object code, Class clazz) {
        if (BasicEnum.class.isAssignableFrom(clazz)) {
            Enum e = getEnum(code, clazz);
            return e == null ? false : true;
        }
        return false;
    }

    /**
     * 指定条件，匹配多个枚举的方法值。若匹配不到，则返回空集合
     * @param predicate 判断条件
     * @param clazz 枚举类
     * @param function 指定方法
     * @param <N> 方法类型
     * @param <E> 枚举类型
     * @return 指定枚举方法值集合
     */
    static <N, E extends Enum<E> & BasicEnum> List<N> getEnumsFun(Predicate<E> predicate, Class<E> clazz, Function<E, N> function) {
        EnumSet<E> all = EnumSet.allOf(clazz);
        return all.stream().filter(predicate).map(function).collect(Collectors.toList());
    }

    /**
     * 根据枚举Code，匹配单个枚举方法值，若无法匹配到，则返回null
     * @param code 枚举Code
     * @param clazz 枚举类
     * @param function 指定方法
     * @param <N> 方法类型
     * @param <K> Code类型
     * @param <E> 枚举类
     * @return 指定枚举方法值
     */
    static <N, K, E extends Enum<E> & BasicEnum> N getEnumFun(K code, Class<E> clazz, Function<E, N> function) {
        return getEnumFun(e -> Objects.equals(e.getCode(), code), clazz, function);
    }

    /**
     * 指定条件，返回枚举对应的方法值。若匹配不到，则返回null
     * @param predicate 判断条件
     * @param clazz 枚举类
     * @param function 指定函数
     * @param <N> 方法类型
     * @param <E> 枚举类型
     * @return 枚举对应方法值
     */
    static <N, E extends Enum<E> & BasicEnum> N getEnumFun(Predicate<E> predicate, Class<E> clazz, Function<E, N> function) {
        EnumSet<E> all = EnumSet.allOf(clazz);
        return all.stream().filter(predicate).findFirst().map(function).orElse(null);
    }

    /**
     * 指定条件，匹配多个枚举。若匹配不到，则返回空集合
     * @param predicate 判断条件
     * @param clazz 枚举类
     * @param <E> 枚举类型
     * @return 枚举集合
     */
    static <E extends Enum<E> & BasicEnum> List<E> getEnums(Predicate<E> predicate, Class<E> clazz) {
        EnumSet<E> all = EnumSet.allOf(clazz);
        return all.stream().filter(predicate).collect(Collectors.toList());
    }


    /**
     * 根据枚举Code，匹配单个枚举，若无法匹配到，则返回null
     * @param code 枚举Code
     * @param clazz 枚举类
     * @param <K> Code 的类型
     * @param <E> 枚举类型
     * @return 枚举
     */
    static <K, E extends Enum<E> & BasicEnum> E getEnum(K code, Class<E> clazz) {
        return getEnum(e -> Objects.equals(e.getCode(), code), clazz);
    }

    /**
     * 指定条件，匹配单个枚举，若无法匹配到，则返回null
     * @param predicate 判断条件
     * @param clazz 枚举类
     * @param <E> 枚举类型
     * @return 枚举集合
     */
    static <E extends Enum<E> & BasicEnum> E getEnum(Predicate<E> predicate, Class<E> clazz) {
        EnumSet<E> all = EnumSet.allOf(clazz);
        return all.stream().filter(predicate).findFirst().orElse(null);
    }

}
