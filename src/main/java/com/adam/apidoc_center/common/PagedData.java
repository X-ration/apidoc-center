package com.adam.apidoc_center.common;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class PagedData<T> {

    private List<T> data;
    private int pageNum;
    private int pageSize;
    private int curPageSize;
    private int totalPage;
    private long totalSize;

    public PagedData(List<T> data, int pageNum, int pageSize, long total) {
        this.data = data;
        this.curPageSize = data.size();
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalSize = total;
        this.totalPage = (int) Math.ceil(1.0 * total / pageSize);
    }

    public static <T> PagedData<T> emptyPagedData(int pageNum, int pageSize) {
        PagedData<T> pagedData = new PagedData<>();
        pagedData.pageNum = pageNum;
        pagedData.pageSize = pageSize;
        pagedData.curPageSize = 0;
        pagedData.totalSize = 0;
        pagedData.totalPage = 0;
        return pagedData;
    }

    public void sort(Comparator<T> comparator) {
        this.data.sort(comparator);
    }

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