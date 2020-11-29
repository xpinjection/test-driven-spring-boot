package com.xpinjection.springboot.adaptors.persistence;

import com.xpinjection.springboot.adaptors.persistence.entity.ExpertEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertDao extends JpaRepository<ExpertEntity, Long> {
}
