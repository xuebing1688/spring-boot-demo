package com.xkcoding.orm.mybatis.plus.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: $
 * @Description:
 * @Author: summer
 * @Date: 2024-09-10 22:42
 * @Version: 1.0
 **/
@Component
public class MybatisPlusUtil {


  /**
   * 创建分页数据
   *
   * @param dataList 全部数据列表
   * @param currentPage 当前页码
   * @param pageSize 每页显示条数
   * @return 分页后的数据
   *
   * 该方法的主要作用是对全量数据进行分页处理，以便在展示时能够分页显示数据
   * 它首先根据当前页码和每页显示条数计算出总记录数，然后根据这些信息设置分页对象，
   * 最后从全量数据中提取出当前页的数据子集，并将其设置到分页对象中
   */
  public IPage<Map<String, Object>> createPagedData(List<Map<String, Object>> dataList, int currentPage, int pageSize) {
    Page<Map<String, Object>> page = new Page<>(currentPage, pageSize);
    int totalRecords = dataList.size();
    page.setTotal(totalRecords);
    int fromIndex = (currentPage - 1) * pageSize;
    int toIndex = Math.min(fromIndex + pageSize, totalRecords);
    List<Map<String, Object>> pageData = dataList.subList(fromIndex, toIndex);
    page.setRecords(pageData);
    return page;
  }


}
