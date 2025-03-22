package com.yoanesber.rate_limit_with_bucket4j.service;

import io.github.bucket4j.*;

public interface RateLimiterService {
    // Resolve the rate limit bucket.
    Bucket resolveBucket(String key);
}
