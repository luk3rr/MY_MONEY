/*
 * Filename: CategoryService.java
 * Created on: October  5, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.services;

import java.util.List;
import java.util.logging.Logger;

import com.mymoney.entities.Category;
import com.mymoney.repositories.CategoryRepository;
import com.mymoney.util.LoggerConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * This class is responsible for managing the categories
 */
@Service
public class CategoryService
{
    @Autowired
    private CategoryRepository categoryRepository;

    private static final Logger m_logger = LoggerConfig.GetLogger();

    public CategoryService() { }

    /**
     * Add a new category
     * @param name Category name
     * @return Category ID
     */
    @Transactional
    public Long AddCategory(String name)
    {
        if (categoryRepository.existsByName(name))
        {
            throw new RuntimeException("Category with name " + name + " already exists");
        }

        Category category = new Category();
        category.SetName(name);

        categoryRepository.save(category);

        m_logger.info("Category " + name + " added successfully");

        return category.GetId();
    }

    /**
     * TODO: Implement this method
     * Delete a category
     * @param id Category ID
     */
    @Transactional
    public void DeleteCategory(Long id)
    {
        // Delete the category from the database
    }

    /**
     * Get all categories
     * @return List of categories
     */
    public List<Category> GetAllCategories()
    {
        return categoryRepository.findAll();
    }
}
