package com.xpinjection.library.adaptors.persistence;

import com.xpinjection.library.adaptors.persistence.entity.ExpertEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertDao extends JpaRepository<ExpertEntity, Long> {
}
