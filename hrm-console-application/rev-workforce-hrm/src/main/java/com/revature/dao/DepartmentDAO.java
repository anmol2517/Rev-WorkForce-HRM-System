package com.revature.dao;

import com.revature.exception.AppException;
import com.revature.model.Department;
import java.util.List;
import java.util.Optional;

public interface DepartmentDAO {
    List<Department> findAll();
    List<Department> getAll() throws AppException;
    Optional<Department> findById(int id);
    int createDepartment(Department department);
    boolean updateDepartment(Department department);
}

