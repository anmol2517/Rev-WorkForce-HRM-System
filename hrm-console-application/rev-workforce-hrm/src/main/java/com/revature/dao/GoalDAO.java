package com.revature.dao;

import com.revature.model.Goal;
import com.revature.model.Goal.GoalStatus;
import java.util.List;
import java.util.Optional;

public interface GoalDAO {
    int createGoal(Goal goal);
    boolean updateGoal(Goal goal);
    boolean updateProgress(int goalId, int progressPercentage, GoalStatus status);
    boolean addManagerGuidance(int goalId, String guidance);
    boolean deleteGoal(int goalId);


    Optional<Goal> findById(int goalId);
    List<Goal> findByEmployee(int employeeId);
    List<Goal> findByEmployeeAndYear(int employeeId, int year);
    List<Goal> findTeamGoalsByManager(int managerId, int year);
    List<Goal> findAllByYear(int year);
}

