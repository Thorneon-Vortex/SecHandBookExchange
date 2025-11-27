package com.tiancai.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页查询结果的封装类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private Long total;    // 总记录数
    private Integer page;      // 当前页码
    private Integer pageSize;  // 每页数量
    private List<T> items; // 当前页数据列表
}