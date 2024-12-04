package java8;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
/**
 * @ClassName: $
 * @Description:
 * @Author: summer
 * @Date: 2024-09-19 21:20
 * @Version: 1.0
 **/
public class MergeList {


    public static void main(String[] args) {
      // 假设 chainResourceList 是已经定义好的 ChainResource 类型的列表
      List<ChainResource> chainResourceList = new ArrayList<>();

      // 调用合并方法
      List<NetworkConfig> configList = mergeConfigLists(chainResourceList);

      // 打印合并后的列表
      configList.forEach(config -> System.out.println(config));
    }

    public static List<NetworkConfig> mergeConfigLists(List<ChainResource> chainResourceList) {
      // 使用 Stream API 来合并所有 ChainResource 中的 NetworkConfig 列表
      return chainResourceList.stream()
        .map(ChainResource::getResourceConfig) // 获取每个 ChainResource 的 ResourceConfig
        .map(ResourceConfig::getConfigList)    // 获取每个 ResourceConfig 的 NetworkConfig 列表
        .flatMap(List::stream)                // 将所有的 NetworkConfig 列表扁平化为一个流
        .collect(Collectors.toList());        // 将流收集到一个新的列表中
    }

  private static class NetworkConfig {
  }


  // 假设的 ChainResource 类
  class ChainResource {
    private ResourceConfig resourceConfig;


    public ChainResource(ResourceConfig resourceConfig) {
      this.resourceConfig = resourceConfig;
    }

    public ResourceConfig getResourceConfig() {
      return resourceConfig;
    }


    public class NetworkConfig {
    }
  }

  // 假设的 ResourceConfig 类
  class ResourceConfig {
    private List<NetworkConfig> configList;

    public ResourceConfig(List<NetworkConfig> configList) {
      this.configList = configList;
    }

    public List<NetworkConfig> getConfigList() {
      return configList;
    }
  }



}
