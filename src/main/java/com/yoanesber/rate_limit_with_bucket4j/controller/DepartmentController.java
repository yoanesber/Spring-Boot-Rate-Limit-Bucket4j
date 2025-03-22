package com.yoanesber.rate_limit_with_bucket4j.controller;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yoanesber.rate_limit_with_bucket4j.entity.CustomHttpResponse;
import com.yoanesber.rate_limit_with_bucket4j.service.DepartmentService;
import com.yoanesber.rate_limit_with_bucket4j.service.RateLimiterService;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    // The postfix for the rate limit key for the find by ID endpoint.
    private static final String RATE_LIMIT_POSTFIX_FIND_ALL = ".find-all-departments";
    private static final String RATE_LIMIT_POSTFIX_FIND_BY_ID = ".find-department-by-id";

    // The services needed for the controller.
    private final DepartmentService departmentService;
    private final RateLimiterService rateLimiterService;

    public DepartmentController(DepartmentService departmentService, 
        RateLimiterService rateLimiterService) {
        this.departmentService = departmentService;
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping
    public ResponseEntity<CustomHttpResponse> getAllDepartments(HttpServletRequest request) {
        try {
            // Use the client unique identifier if available, e.g., user ID instead of IP address.
            Bucket bucket = rateLimiterService.resolveBucket(request.getRemoteAddr() + RATE_LIMIT_POSTFIX_FIND_ALL);

            // Consume 1 token from the bucket every time this endpoint is called.
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            // If the request is not within the limits, return a 429 status code.
            if (probe.isConsumed()) {
                return ResponseEntity.ok(new CustomHttpResponse(HttpStatus.OK.value(), 
                    "All departments retrieved successfully",
                    departmentService.getAllDepartments()));
            } else {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new CustomHttpResponse(HttpStatus.TOO_MANY_REQUESTS.value(), 
                        "Too many requests", 
                        null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CustomHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    e.getMessage(), 
                    null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomHttpResponse> getDepartmentById(@PathVariable String id, HttpServletRequest request) {
        try {
            // Use the client unique identifier if available, e.g., user ID instead of IP address.
            Bucket rateLimitBucket = rateLimiterService.resolveBucket(request.getRemoteAddr() + RATE_LIMIT_POSTFIX_FIND_BY_ID);

            // Consume 1 token from the bucket every time this endpoint is called.
            ConsumptionProbe probe = rateLimitBucket.tryConsumeAndReturnRemaining(1);

            // If the request is not within the limits, return a 429 status code.
            if (probe.isConsumed()) {
                return ResponseEntity.ok(new CustomHttpResponse(HttpStatus.OK.value(), 
                    "Department with ID " + id + " retrieved successfully",
                    departmentService.getDepartmentById(id)));
            } else {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new CustomHttpResponse(HttpStatus.TOO_MANY_REQUESTS.value(), 
                        "Too many requests", 
                        null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CustomHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    e.getMessage(), 
                    null));
        }
    }
}
