package com.xpinjection.springboot.dao.valueobject;

import lombok.NonNull;
import lombok.Value;

@Value
public class Recommendation {
    @NonNull
    private final String sentence;
}
