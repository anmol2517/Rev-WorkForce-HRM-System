package com.revature.dao;

import com.revature.model.Employee;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.sql.SQLException;

public interface EmployeeDAO extends GenericDAO<Employee> {
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByCode(String code);
    List<Employee> search(String keyword);
    List<Employee> findByRole(Employee.Role role) throws SQLException;
    boolean updateManager(int employeeId, int managerId) throws SQLException;
    boolean updateStatus(int employeeId, boolean status) throws SQLException;

    boolean updateProfile(int employeeId, String phone, String address, String emergencyContact);
    boolean setActiveStatus(int employeeId, boolean isActive);
    void assignManager(int empId, int managerId);
    void updatePassword(int employeeId, String newPassword);


    List<Employee> findByManagerId(int managerId);
    List<Employee> findBirthdaysThisMonth();
    List<Employee> findWorkAnniversariesThisMonth();
    String getNextEmployeeCode();
    List<Employee> getManagers();

    boolean emailExists(String email);
    boolean codeExists(String code);
    boolean updateSalaryAndRole(int empId, BigDecimal newSalary, Employee.Role newRole);
}

