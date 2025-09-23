package org.calendar.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface GenericRepository<T, ID> {
    Optional<T> findById(ID id);

    List<T> findAll();

    void save(T entity);

    void update(T entity);

    void delete(ID id);

    T mapRow(ResultSet rs) throws SQLException;
}
