package java8;


import com.alibaba.fastjson.JSON;
import com.example.common.CommonUseApplication;
import com.example.common.model.Menu;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = CommonUseApplication.class)
public class JavaEightTest {


    @Test
    public void test() {
        List<Map<String, String>> list = getList();
        // 筛选出id包含55的数据
        //list.stream().filter(map -> map.get("id").contains("55")).forEach(System.out::prinln);

        List<Integer>  list1 = Lists.newArrayList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24);
        // 遍历出list1中的数据，
         list1.stream().forEach(l->{
             list.stream().filter(map -> map.get("id").equals(l+"")).forEach(System.out::println);
         });


    }


    public List<Map<String,String>>  getList(){
        //往List中 300万数据
        List<Map<String,String>> list = Lists.newArrayList();
        for (int i = 0; i < 6000000; i++) {
            Map<String,String> map = Maps.newHashMap();
            map.put("id",i+"");
            map.put("name","name"+i);
            list.add(map);
        }

        return list;

    }


    /**
    *   测试构建树形结构的方法。
    *   该方法模拟从数据库中查询出一系列菜单项，并将这些菜单项构建成树形结构。
    *   初始数据为一个列表，其中每个菜单项包含菜单ID、菜单名称和父菜单ID。
    *   该方法首先筛选出父菜单项（即那些父菜单ID为0的项），然后为每个父菜单项查找并设置其子菜单项。
    *   最后，将构建好的树形结构以JSON格式输出。
     */
    @Test
    public void testtree(){
        //模拟从数据库查询出来
        List<Menu> menus = Arrays.asList(
            new Menu(1,"根节点",0),
            new Menu(2,"子节点1",1),
            new Menu(3,"子节点1.1",2),
            new Menu(4,"子节点1.2",2),
            new Menu(5,"根节点1.3",2),
            new Menu(6,"根节点2",1),
            new Menu(7,"根节点2.1",6),
            new Menu(8,"根节点2.2",6),
            new Menu(9,"根节点2.2.1",7),
            new Menu(10,"根节点2.2.2",7),
            new Menu(11,"根节点3",1),
            new Menu(12,"根节点3.1",11)
        );

        //获取父节点
        List<Menu> collect = menus.stream().filter(m -> m.getParentId() == 0).map(
            (m) -> {
                m.setChildList(getChildrens(m, menus));
                return m;
            }
        ).collect(Collectors.toList());
        System.out.println("-------转json输出结果-------");
        System.out.println(JSON.toJSON(collect));
    }

    /**
     * 递归查询子节点
     * @param root  根节点
     * @param all   所有节点
     * @return 根节点信息
     */
    private List<Menu> getChildrens(Menu root, List<Menu> all) {
        List<Menu> children = all.stream().filter(m -> {
            return Objects.equals(m.getParentId(), root.getId());
        }).map(
            (m) -> {
                m.setChildList(getChildrens(m, all));
                return m;
            }
        ).collect(Collectors.toList());
        return children;
    }

    @Test
    public  void testCountGroupBySta(){
    List<UserI> list = new ArrayList<>();

    UserI user = new UserI();
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    user.setStartTime(timestamp);
    user.setCount(1);
    user.setSta("sta" + 1);
    list.add(user);

    UserI user1 = new UserI();
    Timestamp timestamp1 = new Timestamp(System.currentTimeMillis());
    user1.setStartTime(timestamp);
    user1.setCount(1);
    user1.setSta("sta" + 1);
    list.add(user1);

    UserI user2 = new UserI();
    Timestamp timestamp2 = new Timestamp(System.currentTimeMillis());
    user2.setStartTime(timestamp2);
    user2.setCount(1);
    user2.setSta("sta" + 2);
    list.add(user2);

    UserI user3 = new UserI();
    Timestamp timestamp3 = new Timestamp(System.currentTimeMillis()-2000);
    user3.setStartTime(timestamp3);
    user3.setCount(1);
    user3.setSta("sta" + 3);
    list.add(user3);

    UserI user4 = new UserI();
    Timestamp timestamp4 = new Timestamp(System.currentTimeMillis()-2000);
    user4.setStartTime(timestamp4);
    user4.setCount(1);
    user4.setSta("sta" + 4);
    list.add(user4);

      List<Map<String, Object>> maps = countGroupBySta(list);
      System.out.println(maps);
    }

  @Data
  public static class UserI {
    private Timestamp startTime;
    private String sta;
    private Integer count;
  }

  public List<Map<String, Object>> countGroupBySta(List<UserI> list) {
    // 根据相同时间把集合对象中相同时间的对象，合并成一个对象，类似以下这种
       /* 把这种  [{
                "startTime": "2024-08-30 11:00:00",
                "status": 3,
                "requestCount": 1
            },
        {
            "startTime": "2024-08-30 11:00:00",
                "status": 1,
                "requestCount": 1581
        } ]

        转成 [{
                "startTime": "2024-08-30 11:00:00",
                value:[{"status": 3,
                "requestCount": 1}],
        {"status": 1,
                "requestCount": 1581}
            }]*/

    Map<Timestamp, List<UserI>> collect = list.stream()
      .collect(Collectors.groupingBy(UserI::getStartTime));

    List<Map<String, Object>> transformedList = collect.entrySet().stream()
      .map(entry -> {
        Map<String, Object> result = new HashMap<>();
        result.put("startTime", entry.getKey());
        result.put("value", entry.getValue().stream()
          .map(dataPoint -> {
            Map<String, Object> valueMap = new HashMap<>();
            valueMap.put("status", dataPoint.getSta());
            valueMap.put("requestCount", dataPoint.getCount());
            return valueMap;
          })
          .collect(Collectors.toList()));
        return result;
      })
      .collect(Collectors.toList());

    return transformedList;
  }


  public static void main(String[] args) {
    // JSON 字符串
    String json = "[{\"logOfKV\":{\"timestamp\":1725432957194,\"message\":\"2024-09-04 14:55:56.833|INFO |traceId=|ScheduledThread|RocketmqClient                          :100|[persistAll] Group: ipd_review_sync_input_consumer_test ClientId: 10.168.1.34@staging-cnbj2-rocketmq.namesrv.api.xiaomi.net:9876@1@2498505289216535 updateConsumeOffsetToBroker MessageQueue [topic=ipd_review_sync_input_test, brokerName=tjwqtst-common-rocketmq-raft0, queueId=0] 199\",\"logip\":\"10.168.1.34\",\"tail\":\"matrix_ipd-review-service\",\"tailId\":121380},\"logOfString\":\"{\\\"timestamp\\\":1725432957194,\\\"message\\\":\\\"2024-09-04 14:55:56.833|INFO |traceId=|ScheduledThread|RocketmqClient                          :100|[persistAll] Group: ipd_review_sync_input_consumer_test ClientId: 10.168.1.34@staging-cnbj2-rocketmq.namesrv.api.xiaomi.net:9876@1@2498505289216535 updateConsumeOffsetToBroker MessageQueue [topic=ipd_review_sync_input_test, brokerName=tjwqtst-common-rocketmq-raft0, queueId=0] 199\\\",\\\"logip\\\":\\\"10.168.1.34\\\",\\\"tail\\\":\\\"matrix_ipd-review-service\\\",\\\"tailId\\\":121380}\",\"fileName\":\"/home/work/log/ipd-review-service-22354-ipd-review-service-test/ipd-review-service-test-dd6s9/logs/ipd-review-service/info.log\",\"lineNumber\":\"27525\",\"ip\":\"10.168.1.34\",\"timestamp\":\"1725432957194\",\"highlight\":{}},{\"logOfKV\":{\"timestamp\":1725432957194,\"message\":\"2024-09-04 14:55:56.995|INFO |traceId=|ScheduledThread|RocketmqClient                          :100|[persistAll] Group: ipd-checklistTemplate-consumer-test ClientId: 10.168.1.34@staging-cnbj2-rocketmq.namesrv.api.xiaomi.net:9876@1@2498505451381016 updateConsumeOffsetToBroker MessageQueue [topic=%RETRY%ipd-checklistTemplate-consumer-test, brokerName=tjwqtst-common-rocketmq-raft0, queueId=0] 28\",\"logip\":\"10.168.1.34\",\"tail\":\"matrix_ipd-review-service\",\"tailId\":121380},\"logOfString\":\"{\\\"timestamp\\\":1725432957194,\\\"message\\\":\\\"2024-09-04 14:55:56.995|INFO |traceId=|ScheduledThread|RocketmqClient                          :100|[persistAll] Group: ipd-checklistTemplate-consumer-test ClientId: 10.168.1.34@staging-cnbj2-rocketmq.namesrv.api.xiaomi.net:9876@1@2498505451381016 updateConsumeOffsetToBroker MessageQueue [topic=%RETRY%ipd-checklistTemplate-consumer-test, brokerName=tjwqtst-common-rocketmq-raft0, queueId=0] 28\\\",\\\"logip\\\":\\\"10.168.1.34\\\",\\\"tail\\\":\\\"matrix_ipd-review-service\\\",\\\"tailId\\\":121380}\",\"fileName\":\"/home/work/log/ipd-review-service-22354-ipd-review-service-test/ipd-review-service-test-dd6s9/logs/ipd-review-service/info.log\",\"lineNumber\":\"27526\",\"ip\":\"10.168.1.34\",\"timestamp\":\"1725432957194\",\"highlight\":{}},{\"logOfKV\":{\"timestamp\":1725432957194,\"message\":\"2024-09-04 14:55:56.995|INFO |traceId=|ScheduledThread|RocketmqClient                          :100|[persistAll] Group: ipd-checklistTemplate-consumer-test ClientId: 10.168.1.34@staging-cnbj2-rocketmq.namesrv.api.xiaomi.net:9876@1@2498505451381016 updateConsumeOffsetToBroker MessageQueue [topic=%RETRY%ipd-checklistTemplate-consumer-test, brokerName=tjwqtst-common-rocketmq-raft0, queueId=0] 28\",\"logip\":\"10.168.1.34\",\"tail\":\"matrix_ipd-review-service\",\"tailId\":121380},\"logOfString\":\"{\\\"timestamp\\\":1725432957194,\\\"message\\\":\\\"2024-09-04 14:55:56.995|INFO |traceId=|ScheduledThread|RocketmqClient                          :100|[persistAll] Group: ipd-checklistTemplate-consumer-test ClientId: 10.168.1.34@staging-cnbj2-rocketmq.namesrv.api.xiaomi.net:9876@1@2498505451381016 updateConsumeOffsetToBroker MessageQueue [topic=%RETRY%ipd-checklistTemplate-consumer-test, brokerName=tjwqtst-common-rocketmq-raft0, queueId=0] 28\\\",\\\"logip\\\":\\\"10.168.1.34\\\",\\\"tail\\\":\\\"matrix_ipd-review-service\\\",\\\"tailId\\\":121380}\",\"fileName\":\"/home/work/log/ipd-review-service-22354-ipd-review-service-test/ipd-review-service-test-dd6s9/logs/ipd-review-service/info.log\",\"lineNumber\":\"27527\",\"ip\":\"10.168.1.34\",\"timestamp\":\"1725432957194\",\"highlight\":{}},{\"logOfKV\":{\"timestamp\":1725432957194,\"message\":\"2024-09-04 14:55:56.995|INFO |traceId=|ScheduledThread|RocketmqClient                          :100|[persistAll] Group: ipd-checklistTemplate-consumer-test ClientId: 10.168.1.34@staging-cnbj2-rocketmq.namesrv.api.xiaomi.net:9876@1@2498505451381016 updateConsumeOffsetToBroker MessageQueue [topic=ipd-windchill-checklistTemplate-event, brokerName=tjwqtst-common-rocketmq-raft0, queueId=0] 47\",\"logip\":\"10.168.1.34\",\"tail\":\"matrix_ipd-review-service\",\"tailId\":121380},\"logOfString\":\"{\\\"timestamp\\\":1725432957194,\\\"message\\\":\\\"2024-09-04 14:55:56.995|INFO |traceId=|ScheduledThread|RocketmqClient                          :100|[persistAll] Group: ipd-checklistTemplate-consumer-test ClientId: 10.168.1.34@staging-cnbj2-rocketmq.namesrv.api.xiaomi.net:9876@1@2498505451381016 updateConsumeOffsetToBroker MessageQueue [topic=ipd-windchill-checklistTemplate-event, brokerName=tjwqtst-common-rocketmq-raft0, queueId=0] 47\\\",\\\"logip\\\":\\\"10.168.1.34\\\",\\\"tail\\\":\\\"matrix_ipd-review-service\\\",\\\"tailId\\\":121380}\",\"fileName\":\"/home/work/log/ipd-review-service-22354-ipd-review-service-test/ipd-review-service-test-dd6s9/logs/ipd-review-service/info.log\",\"lineNumber\":\"27528\",\"ip\":\"10.168.1.34\",\"timestamp\":\"1725432957194\",\"highlight\":{}},{\"logOfKV\":{\"timestamp\":1725432956893,\"message\":\"2024-09-04 14:55:56.565|INFO |traceId=|ScheduledThread|RocketmqClient                          :100|[persistAll] Group: cg_review_created_test ClientId: 10.168.1.34@staging-cnbj2-rocketmq.namesrv.api.xiaomi.net:9876@1@2498504990671740 updateConsumeOffsetToBroker MessageQueue [topic=tp_task_start_test, brokerName=tjwqtst-common-rocketmq-raft0, queueId=0] 11\",\"logip\":\"10.168.1.34\",\"tail\":\"matrix_ipd-review-service\",\"tailId\":121380},\"logOfString\":\"{\\\"timestamp\\\":1725432956893,\\\"message\\\":\\\"2024-09-04 14:55:56.565|INFO |traceId=|ScheduledThread|RocketmqClient                          :100|[persistAll] Group: cg_review_created_test ClientId: 10.168.1.34@staging-cnbj2-rocketmq.namesrv.api.xiaomi.net:9876@1@2498504990671740 updateConsumeOffsetToBroker MessageQueue [topic=tp_task_start_test, brokerName=tjwqtst-common-rocketmq-raft0, queueId=0] 11\\\",\\\"logip\\\":\\\"10.168.1.34\\\",\\\"tail\\\":\\\"matrix_ipd-review-service\\\",\\\"tailId\\\":121380}\",\"fileName\":\"/home/work/log/ipd-review-service-22354-ipd-review-service-test/ipd-review-service-test-dd6s9/logs/ipd-review-service/info.log\",\"lineNumber\":\"27513\",\"ip\":\"10.168.1.34\",\"timestamp\":\"1725432956893\",\"highlight\":{}},{\"logOfKV\":{\"timestamp\":1725432956893,\"message\":\"2024-09-04 14:55:56.691|INFO |traceId=|ScheduledThread|RocketmqClient                          :100|[persistAll] Group: ipd-checklist-consumer-test ClientId: 10.168.1.34@staging-cnbj2-rocketmq.namesrv.api.xiaomi.net:9876@1@2498505147441571 updateConsumeOffsetToBroker MessageQueue [topic=ipd-windchill-checklist-event, brokerName=tjwqtst-common-rocketmq-raft0, queueId=0] 64\",\"logip\":\"10.168.1.34\",\"tail\":\"matrix_ipd-review-service\",\"tailId\":121380},\"logOfString\":\"{\\\"timestamp\\\":1725432956893,\\\"message\\\":\\\"2024-09-04 14:55:56.691|INFO |traceId=|ScheduledThread|RocketmqClient                          :100|[persistAll] Group: ipd-checklist-consumer-test ClientId: 10.168.1.34@staging-cnbj2-rocketmq.namesrv.api.xiaomi.net:9876@1@2498505147441571 updateConsumeOffsetToBroker MessageQueue [topic=ipd-windchill-checklist-event, brokerName=tjwqtst-common-rocketmq-raft0, queueId=0] 64\\\",\\\"logip\\\":\\\"10.168.1.34\\\",\\\"tail\\\":\\\"matrix_ipd-review-service\\\",\\\"tailId\\\":121380}\",\"fileName\":\"/home/work/log/ipd-review-service-22354-ipd-review-service-test/ipd-review-service-test-dd6s9/logs/ipd-review-service/info.log\",\"lineNumber\":\"27514\",\"ip\":\"10.168.1.34\",\"timestamp\":\"1725432956893\",\"highlight\":{}},{\"logOfKV\":{\"timestamp\":1725432956893,\"message\":\"2024-09-04 14:55:56.691|INFO |traceId=|ScheduledThread|RocketmqClient                          :100|[persistAll] Group: ipd-checklist-consumer-test ClientId: 10.168.1.34@staging-cnbj2-rocketmq.namesrv.api.xiaomi.net:9876@1@2498505147441571 updateConsumeOffsetToBroker MessageQueue [topic=ipd-windchill-checklist-event, brokerName=tjwqtst-common-rocketmq-raft0, queueId=0] 64\",\"logip\":\"10.168.1.34\",\"tail\":\"matrix_ipd-review-service\",\"tailId\":121380},\"logOfString\":\"{\\\"timestamp\\\":1725432956893,\\\"message\\\":\\\"2024-09-04 14:55:56.691|INFO |traceId=|ScheduledThread|RocketmqClient                          :100|[persistAll] Group: ipd-checklist-consumer-test ClientId: 10.168.1.34@staging-cnbj2-rocketmq.namesrv.api.xiaomi.net:9876@1@2498505147441571 updateConsumeOffsetToBroker MessageQueue [topic=ipd-windchill-checklist-event, brokerName=tjwqtst-common-rocketmq-raft0, queueId=0] 64\\\",\\\"logip\\\":\\\"10.168.1.34\\\",\\\"tail\\\":\\\"matrix_ipd-review-service\\\",\\\"tailId\\\":121380}\",\"fileName\":\"/home/work/log/ipd-review-service-22354-ipd-review-service-test/ipd-review-service-test-dd6s9/logs/ipd-review-service/info.log\",\"lineNumber\":\"27515\",\"ip\":\"10.168.1.34\",\"timestamp\":\"1725432956893\",\"highlight\":{}},{\"logOfKV\":{\"timestamp\":1725432956893,\"message\":\"2024-09-04 14:55:56.691|INFO |traceId=|ScheduledThread|RocketmqClient                          :100|[persistAll] Group: ipd-checklist-consumer-test ClientId: 10.168.1.34@staging-cnbj2-rocketmq.namesrv.api.xiaomi.net:9876@1@2498505147441571 updateConsumeOffsetToBroker MessageQueue [topic=%RETRY%ipd-checklist-consumer-test, brokerName=tjwqtst-common-rocketmq-raft0, queueId=0] 171\",\"logip\":\"10.168.1.34\",\"tail\":\"matrix_ipd-review-service\",\"tailId\":121380},\"logOfString\":\"{\\\"timestamp\\\":1725432956893,\\\"message\\\":\\\"2024-09-04 14:55:56.691|INFO |traceId=|ScheduledThread|RocketmqClient                          :100|[persistAll] Group: ipd-checklist-consumer-test ClientId: 10.168.1.34@staging-cnbj2-rocketmq.namesrv.api.xiaomi.net:9876@1@2498505147441571 updateConsumeOffsetToBroker MessageQueue [topic=%RETRY%ipd-checklist-consumer-test, brokerName=tjwqtst-common-rocketmq-raft0, queueId=0] 171\\\",\\\"logip\\\":\\\"10.168.1.34\\\",\\\"tail\\\":\\\"matrix_ipd-review-service\\\",\\\"tailId\\\":121380}\",\"fileName\":\"/home/work/log/ipd-review-service-22354-ipd-review-service-test/ipd-review-service-test-dd6s9/logs/ipd-review-service/info.log\",\"lineNumber\":\"27516\",\"ip\":\"10.168.1.34\",\"timestamp\":\"1725432956893\",\"highlight\":{}},{\"logOfKV\":{\"timestamp\":1725432956893,\"message\":\"2024-09-04 14:55:56.691|INFO |traceId=|ScheduledThread|RocketmqClient                          :100|[persistAll] Group: ipd-checklist-consumer-test ClientId: 10.168.1.34@staging-cnbj2-rocketmq.namesrv.api.xiaomi.net:9876@1@2498505147441571 updateConsumeOffsetToBroker MessageQueue [topic=%RETRY%ipd-checklist-consumer-test, brokerName=tjwqtst-common-rocketmq-raft0, queueId=0] 171\",\"logip\":\"10.168.1.34\",\"tail\":\"matrix_ipd-review-service\",\"tailId\":121380},\"logOfString\":\"{\\\"timestamp\\\":1725432956893,\\\"message\\\":\\\"2024-09-04 14:55:56.691|INFO |traceId=|ScheduledThread|RocketmqClient                          :100|[persistAll] Group: ipd-checklist-consumer-test ClientId: 10.168.1.34@staging-cnbj2-rocketmq.namesrv.api.xiaomi.net:9876@1@2498505147441571 updateConsumeOffsetToBroker MessageQueue [topic=%RETRY%ipd-checklist-consumer-test, brokerName=tjwqtst-common-rocketmq-raft0, queueId=0] 171\\\",\\\"logip\\\":\\\"10.168.1.34\\\",\\\"tail\\\":\\\"matrix_ipd-review-service\\\",\\\"tailId\\\":121380}\",\"fileName\":\"/home/work/log/ipd-review-service-22354-ipd-review-service-test/ipd-review-service-test-dd6s9/logs/ipd-review-service/info.log\",\"lineNumber\":\"27517\",\"ip\":\"10.168.1.34\",\"timestamp\":\"1725432956893\",\"highlight\":{}},{\"logOfKV\":{\"timestamp\":1725432956893,\"message\":\"2024-09-04 14:55:56.758|INFO |traceId=|ScheduledThread|RocketmqClient                          :100|[persistAll] Group: review_consume_workflowevent_group_test ClientId: 10.168.1.34@staging-cnbj2-rocketmq.namesrv.api.xiaomi.net:9876@1@2498505214259475 updateConsumeOffsetToBroker MessageQueue [topic=ipd-workflow-event-test, brokerName=tjwqtst-common-rocketmq-raft0, queueId=0] 20350\",\"logip\":\"10.168.1.34\",\"tail\":\"matrix_ipd-review-service\",\"tailId\":121380},\"logOfString\":\"{\\\"timestamp\\\":1725432956893,\\\"message\\\":\\\"2024-09-04 14:55:56.758|INFO |traceId=|ScheduledThread|RocketmqClient                          :100|[persistAll] Group: review_consume_workflowevent_group_test ClientId: 10.168.1.34@staging-cnbj2-rocketmq.namesrv.api.xiaomi.net:9876@1@2498505214259475 updateConsumeOffsetToBroker MessageQueue [topic=ipd-workflow-event-test, brokerName=tjwqtst-common-rocketmq-raft0, queueId=0] 20350\\\",\\\"logip\\\":\\\"10.168.1.34\\\",\\\"tail\\\":\\\"matrix_ipd-review-service\\\",\\\"tailId\\\":121380}\",\"fileName\":\"/home/work/log/ipd-review-service-22354-ipd-review-service-test/ipd-review-service-test-dd6s9/logs/ipd-review-service/info.log\",\"lineNumber\":\"27518\",\"ip\":\"10.168.1.34\",\"timestamp\":\"1725432956893\",\"highlight\":{}}]" ;
      // 创建 ObjectMapper 实例
    ObjectMapper objectMapper = new ObjectMapper();

    // 将 JSON 字符串转换为 List<Map<String, Object>>
    List<Map<String, Object>> list = null;
    try {
      list = objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    // 打印转换后的 List<Map<String, Object>>
    list.forEach(map -> System.out.println(map));

  }

  // java8 String类型的时间yyyy-MM-dd HH:mm:ss 转成timestamp的方法
  @Test
  public void test2() throws ParseException {
    String timeString = "2024-09-04 14:20:22";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime localDateTime = LocalDateTime.parse(timeString, formatter);
    Timestamp timestamp = Timestamp.valueOf(localDateTime);
    System.out.println("Timestamp: " + timestamp);
  }



}
