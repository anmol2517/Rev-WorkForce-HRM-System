package com.revature.dao;

import com.revature.exception.AppException;
import com.revature.model.Designation;
import java.util.List;
import java.util.Optional;

public interface DesignationDAO {
    List<Designation> findAll();
    List<Designation> getAll() throws AppException;
    Optional<Designation> findById(int id);
    int createDesignation(Designation designation);
    boolean updateDesignation(Designation designation);
}

