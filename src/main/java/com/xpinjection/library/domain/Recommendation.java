package com.xpinjection.library.domain;

import lombok.NonNull;
import lombok.Value;

@Value
public class Recommendation {
    @NonNull
    String sentence;
}
