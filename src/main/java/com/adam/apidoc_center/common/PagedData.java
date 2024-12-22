package com.adam.apidoc_center.common;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
public class PagedData<T> {

    private List<T> data;
    private int pageNum;
    private int pageSize;
    private int curPageSize;
    private int totalPage;
    private long totalSize;

    public static <T> PagedData<T> convert(Page<T> page, Pageable pageable) {
        PagedData<T> pagedData = new PagedData<>();
        pagedData.data = page.getContent();
        pagedData.pageNum = pageable.getPageNumber();
        pagedData.pageSize = pageable.getPageSize();
        pagedData.curPageSize = page.getContent().size();
        pagedData.totalPage = page.getTotalPages();
        pagedData.totalSize = page.getTotalElements();
        return pagedData;
    }

    public <R> PagedData<R> map(Function<T,R> function) {
        PagedData<R> pagedData = new PagedData<>();
        pagedData.pageNum = this.pageNum;
        pagedData.pageSize = this.pageSize;
        pagedData.curPageSize = this.curPageSize;
        pagedData.totalPage = this.totalPage;
        pagedData.totalSize = this.totalSize;
        if(this.data == null) {
            return pagedData;
        }
        pagedData.data = this.data.stream().map(function).collect(Collectors.toList());
        return pagedData;
    }

}