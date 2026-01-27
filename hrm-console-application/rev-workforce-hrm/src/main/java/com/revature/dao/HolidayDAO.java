package com.revature.dao;

import com.revature.model.Holiday;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface HolidayDAO extends GenericDAO<Holiday> {
    List<Holiday> findByYear(int year) throws SQLException;
    List<Holiday> findUpcoming() throws SQLException;


    int createHoliday(Holiday holiday) throws SQLException;
    boolean updateHoliday(Holiday holiday) throws SQLException;
    boolean deleteHoliday(int holidayId) throws SQLException;
}

