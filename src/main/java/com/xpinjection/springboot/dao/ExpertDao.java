package com.xpinjection.springboot.dao;

import com.xpinjection.springboot.dao.entity.ExpertEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertDao extends JpaRepository<ExpertEntity, Long> {
}
