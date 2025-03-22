package com.yoanesber.rate_limit_with_bucket4j.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

import com.yoanesber.rate_limit_with_bucket4j.entity.Department;
import com.yoanesber.rate_limit_with_bucket4j.service.DepartmentService;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Override
    public List<Department> getAllDepartments() {
        // This is a dummy implementation
        List<Department> departments = new ArrayList<>();
        Department department1 = new Department();
        department1.setId("1");
        department1.setDeptName("IT");
        department1.setActive(true);
        department1.setCreatedBy(1L);
        department1.setCreatedDate(LocalDateTime.now());
        department1.setUpdatedBy(1L);
        department1.setUpdatedDate(LocalDateTime.now());
        departments.add(department1);
        Department department2 = new Department();
        department2.setId("2");
        department2.setDeptName("HR");
        department2.setActive(true);
        department2.setCreatedBy(1L);
        department2.setCreatedDate(LocalDateTime.now());
        department2.setUpdatedBy(1L);
        department2.setUpdatedDate(LocalDateTime.now());
        departments.add(department2);
        return departments;
    }

    @Override
    public Department getDepartmentById(String id) {
        // This is a dummy implementation
        Department department = new Department();
        department.setId(id);
        department.setDeptName("IT");
        department.setActive(true);
        department.setCreatedBy(1L);
        department.setCreatedDate(LocalDateTime.now());
        department.setUpdatedBy(1L);
        department.setUpdatedDate(LocalDateTime.now());
        return department;
    }

}
