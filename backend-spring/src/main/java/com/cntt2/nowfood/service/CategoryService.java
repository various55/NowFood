package com.cntt2.nowfood.service;

import com.cntt2.nowfood.domain.Category;
import com.cntt2.nowfood.dto.SearchDto;
import com.cntt2.nowfood.dto.category.CategoryDto;
import com.cntt2.nowfood.dto.category.CategoryFormDto;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @author Vanh
 * @version 1.0
 * @date 11/3/2021 10:23 PM
 */
public interface CategoryService extends GenericService<Category, Integer> {
    List<Category>  getAll();
    Category findById(Integer id);
    Category saveOrUpdate(CategoryFormDto form);
    Page<CategoryDto> findByAdvSearch(SearchDto dto);
}
