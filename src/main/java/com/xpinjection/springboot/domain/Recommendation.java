package com.xpinjection.springboot.domain;

import lombok.NonNull;
import lombok.Value;

@Value
public class Recommendation {
    @NonNull
    String sentence;
}
