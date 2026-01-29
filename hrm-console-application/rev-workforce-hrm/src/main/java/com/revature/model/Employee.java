package com.revature.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


//  Employee Entity - Core model representing an employee in the system

public class Employee {
    private int employeeId;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private String emergencyContact;
    private int departmentId;
    private int designationId;
    private Integer managerId;             //     -----     Nullable for top-level employees
    private Role role;
    private BigDecimal salary;
    private LocalDate joiningDate;
    private boolean isActive;
    private boolean isDeleted;
    private String securityQuestion;
    private String securityAnswer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    

//     Additional fields for display (from JOINs)

    private String departmentName;
    private String designationName;
    private String managerName;

    public enum Role {
        ADMIN, MANAGER, EMPLOYEE
    }

    public Employee() {
        this.role = Role.EMPLOYEE;
        this.isActive = true;
        this.isDeleted = false;
        this.joiningDate = LocalDate.now();
    }

    public Employee(String firstName, String lastName, String email, String password)
    {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    public Employee(int employeeId, String employeeCode, String firstName, String lastName, 
                   String email, String password, String phone, String address,
                   LocalDate dateOfBirth, String emergencyContact, int departmentId,
                   int designationId, Integer managerId, Role role, BigDecimal salary,
                   LocalDate joiningDate, boolean isActive, boolean isDeleted)
    {
        this.employeeId = employeeId;
        this.employeeCode = employeeCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.emergencyContact = emergencyContact;
        this.departmentId = departmentId;
        this.designationId = designationId;
        this.managerId = managerId;
        this.role = role;
        this.salary = salary;
        this.joiningDate = joiningDate;
        this.isActive = isActive;
        this.isDeleted = isDeleted;
    }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() { return firstName + " " + lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

    public int getDesignationId() { return designationId; }
    public void setDesignationId(int designationId) { this.designationId = designationId; }

    public Integer getManagerId() { return managerId; }
    public void setManagerId(Integer managerId) { this.managerId = managerId; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public LocalDate getDateOfJoining() { return joiningDate; }
    public void setDateOfJoining(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public String getSecurityQuestion() { return securityQuestion; }
    public void setSecurityQuestion(String securityQuestion) { this.securityQuestion = securityQuestion; }

    public String getSecurityAnswer() { return securityAnswer; }
    public void setSecurityAnswer(String securityAnswer) { this.securityAnswer = securityAnswer; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getDesignationName() { return designationName; }
    public void setDesignationName(String designationName) { this.designationName = designationName; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    @Override
    public String toString() {
        return String.format("Employee[%s] %s - %s (%s)", 
            employeeCode, getFullName(), email, role);
    }

    public boolean isManager() {
        return this.role == Role.MANAGER || this.role == Role.ADMIN;
    }
}


