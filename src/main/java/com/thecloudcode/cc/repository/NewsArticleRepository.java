package com.thecloudcode.cc.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.thecloudcode.cc.models.NewsArticle;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle,Long> {


     List<NewsArticle> findByCreatedAtAfter(LocalDateTime date);
    
    List<NewsArticle> findBySourceOrderByPublishedDateDesc(String source);
    
    @Query("SELECT a FROM NewsArticle a WHERE a.createdAt >= :date ORDER BY a.publishedDate DESC")
    List<NewsArticle> findRecentArticles(LocalDateTime date);
    
    boolean existsByUrl(String url);

}
