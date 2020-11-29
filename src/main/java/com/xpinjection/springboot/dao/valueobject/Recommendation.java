package com.xpinjection.springboot.dao.valueobject;

import lombok.NonNull;
import lombok.Value;

@Value
public class Recommendation {
    @NonNull
    String sentence;
}
