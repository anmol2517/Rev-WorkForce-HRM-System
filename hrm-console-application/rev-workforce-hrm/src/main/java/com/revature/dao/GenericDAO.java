package com.revature.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface GenericDAO<T> {
    T create(T t) throws SQLException;
    Optional<T> findById(int id) throws SQLException;
    List<T> findAll() throws SQLException;
    boolean update(T t) throws SQLException;
    boolean delete(int id) throws SQLException;
}

