package com.revature.util;

import com.revature.model.Employee;


//    Session manager for maintaining current user session  ||  Singleton pattern implementation

public class SessionManager {
    
    private static SessionManager instance;
    private Employee currentUser;
    private long loginTime;

    private SessionManager() {
    }
    

//       Get singleton instance  ||  @return SessionManager instance

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }


    public void setCurrentUser(Employee employee) {
        this.currentUser = employee;
        this.loginTime = System.currentTimeMillis();
    }
    

    public Employee getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getEmployeeId() : -1;
    }
    

//     Get current user's role  ||  @return role string or null if not logged in

    public String getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole().name() : "";
    }
    

//      Get current user's full name  ||  @return full name or null if not logged in

    public String getCurrentUserFullName() {
        return currentUser != null ? currentUser.getFullName() : null;
    }


//     if current user is Admin  ||  @return true if admin

    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Employee.Role.ADMIN;
    }

    public boolean isManager() {
        return currentUser != null && currentUser.getRole() == Employee.Role.MANAGER;
    }

    public boolean isEmployee() {
        return currentUser != null && currentUser.getRole() == Employee.Role.EMPLOYEE;
    }


//       Get login time  ||  @return login timestamp in milliseconds

    public long getLoginTime() {
        return loginTime;
    }
    

//      Get session duration in minutes  ||  @return session duration in minutes

    public long getSessionDurationMinutes() {
        if (!isLoggedIn()) {
            return 0;
        }
        return (System.currentTimeMillis() - loginTime) / 60000;
    }


//      Clear session (logout)

    public void clearSession() {
        currentUser = null;
        loginTime = 0;
    }


//      Destroy session manager instance (for testing purposes)

    public static void destroyInstance() {
        if (instance != null) {
            instance.clearSession();
            instance = null;
        }
    }
}


