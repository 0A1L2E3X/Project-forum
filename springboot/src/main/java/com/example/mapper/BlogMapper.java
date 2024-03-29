package com.example.mapper;

import com.example.entity.Blog;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BlogMapper {

    /**
     * 新增
     */
    int insert(Blog blog);

    /**
     * 删除
     */
    int deleteById(Integer id);

    /**
     * 修改
     */
    int updateById(Blog blog);

    /**
     * 根据ID查询
     */
    Blog selectById(Integer id);

    /**
     * 查询所有
     */
    List<Blog> selectAll(Blog blog);

    @Select("select * from blog where user_id = #{userID}")
    List<Blog> selectUserBlog(Integer userID);

    List<Blog> selectComment(Blog blog);

    List<Blog> selectLike(Blog blog);

    List<Blog> selectCollect(Blog blog);
}
