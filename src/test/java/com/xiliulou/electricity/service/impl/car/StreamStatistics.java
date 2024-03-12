package com.xiliulou.electricity.service.impl.car;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Test;


public class StreamStatistics {
    
    @Test
    public void testamentAsia() {

        Student student1 = new Student(1, 1);
        Student student2 = new Student(1, 1);
        Student student3 = new Student(2, 2);
        Student student4 = new Student(2, 3);
        Student student5 = new Student(3, 3);
        Student student6 = new Student(3, 4);
        Student student7 = new Student(4, 1);
        Student student8 = new Student(4, 1);
        Student student9 = new Student(4, 2);
        Student student10 = new Student(4, 1);
        Student student11 = new Student(5);

        
        List<Student> list = Arrays.asList(student1, student2, student3, student4, student5, student6, student7, student8, student9, student10,student11);
        
        
        
        System.out.println("--------- 根据其他排序 ----------");
        Map<Integer, List<Student>> collect1 = list.parallelStream().collect(Collectors.groupingBy(Student::getId));
        System.out.println(JSONObject.toJSONString(collect1));
        
        System.out.println("--------- 根据字段分组，求每个分组的sum ----------");
        Map<Integer, Integer> collect = list.stream().collect(Collectors.groupingBy(Student::getId, Collectors.summingInt(Student::getScore)));
        System.out.println(collect.toString());

        System.out.println("--------- 根据字段分组，求每个分组的count ----------");
        Map<Integer, Long> countMap = list.stream().collect(Collectors.groupingBy(Student::getId, Collectors.counting()));
        System.out.println(countMap.toString());

        System.out.println("--------- 根据字段分组，每个分组为：对象的指定字段 ----------");
        Map<Integer, List<Integer>> groupMap = list.stream().collect(Collectors.groupingBy(Student::getId, Collectors.mapping(Student::getScore, Collectors.toCollection(ArrayList::new))));
        System.out.println(groupMap.toString());

        System.out.println("--------- 根据字段分组,默认分组 ----------");
        Map<Integer, List<Student>> defaultGroupMap = list.stream().collect(Collectors.groupingBy(Student::getId));
        System.out.println(JSONObject.toJSONString(defaultGroupMap));

        System.out.println("--------- 根据字段分组，每个分组按照指定字段进行生序排序 ----------");
        Map<Integer, List<Student>> sortGroupMap = list.stream().sorted(Comparator.comparing(Student::getScore))
                .collect(Collectors.groupingBy(Student::getId));
        System.out.println(JSONObject.toJSONString(sortGroupMap));

        System.out.println("--------- 先排序，再分组 ----------");
        Map<Integer, List<Student>> reversedSortGroupMap = list.stream().sorted(Comparator.comparing(Student::getScore).reversed())
                .collect(Collectors.groupingBy(Student::getId));
        System.out.println(JSONObject.toJSONString(reversedSortGroupMap));
        
      

    }
}

class Student {

    private Integer id;

    private Integer score;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Student(Integer id, Integer score) {
        this.id = id;
        this.score = score;
    }
    
    public Student(Integer id) {
        this.id = id;
    }
}
