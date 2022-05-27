package com.xpinjection.library.service;

import com.xpinjection.library.domain.dto.CreateExpertDto;

public interface ExpertService {
    long addExpert(CreateExpertDto expert);
}
