package com.example.ice.test;
import com.hankcs.hanlp.HanLP;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SecondWord {

    @Test
    public  void  segment() {
        String text="电网公平开放监管办法";
        List<String> collect = HanLP.segment(text)
            .stream()
            .map(term -> term.word)
            .collect(Collectors.toList());

        collect.stream().forEach(System.out::println);
    }


    public Map  test01() {
       //生成 一个Map<String,List<String,Object>> 格式的测试数据
        Map<String,List<Map<String,Object>>> map = new HashMap<>();
         List<Map<String,Object>> list = new ArrayList<>();
            Map<String,Object> map1 = new HashMap<>();
            map1.put("name","张三:12");
            map1.put("age",12);
            list.add(map1);
            map.put("list",list);
        List<Map<String,Object>> list2 = new ArrayList<>();
        Map<String,Object> map2 = new HashMap<>();
        map1.put("name","张三:12");
        map1.put("age",155);
        list2.add(map1);
        map.put("list2",list2);
        return map;
    }

    @Test
    public void test02(){


    }

    public   Map<String,List> getList(){
        Map map = test01();
        List<Map<String,Object>> list = (List<Map<String, Object>>) map.get("list");
        List<Map<String,Object>> list2 = (List<Map<String, Object>>)map.get("list2");

        List<Map<String,Object>> newList = new ArrayList<>(list2);
        list.stream().forEach(map2->{
            //  把causeClass 按冒号分割成不同的字符串
            String[] causeClass = map2.get("name").toString().split(":");
            //  把causeClass的第一个字符串赋值给type
            map2.put("name",causeClass[0]);
            //  把causeClass的第二个字符串赋值给causeClass 并去掉括号
            map2.put("causeClass",causeClass[1].substring(1,causeClass[1].length()-1));
        });


        //  把growthRateTopThree的causeClass 按冒号分割成不同的字符串
        Stream<Map<String, Object>> stream = newList.stream();
        stream.forEach(map2->{
            // 把causeClass 按冒号分割成不同的字符串
            String[] causeClass = map2.get("name").toString().split(":");
            // 把causeClass的第一个字符串赋值给type
            map2.put("name",causeClass[0]);
            // 把causeClass的第二个字符串赋值给causeClass 并去掉括号
            map2.put("causeClass",causeClass[1].substring(1,causeClass[1].length()-1));
        });
        list = newList;

        map.put("list",list);
        map.put("list2",list2);
        return map;
    }


 }
