package com.cookbook.repository;

import com.cookbook.entity.AiConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiConfigRepository extends JpaRepository<AiConfig, Long> {
    
    Optional<AiConfig> findByIsActiveTrue();
    
    List<AiConfig> findByIsPresetTrue();
    
    List<AiConfig> findByIsPresetFalse();
    
    Optional<AiConfig> findByName(String name);
    
    boolean existsByName(String name);
}
