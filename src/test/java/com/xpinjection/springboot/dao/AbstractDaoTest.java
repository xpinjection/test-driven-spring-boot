package com.xpinjection.springboot.dao;

import com.github.database.rider.core.DBUnitRule;
import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Alimenkou Mikalai
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public abstract class AbstractDaoTest<D> {
    private static long ID = 1;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance(() -> jdbcTemplate.getDataSource().getConnection());

    @Autowired
    protected TestEntityManager em;

    @Autowired
    protected D dao;

    protected long addRecordToDatabase(String table, Map<String, Object> fields) {
        long id = ID++;
        Object[] params = Stream.concat(Stream.of(id), fields.values().stream()).toArray();
        jdbcTemplate.update("INSERT INTO " + table +
                " (id, " + String.join(", ", fields.keySet()) +
                ") VALUES (?" + StringUtils.repeat(", ?", fields.size()) + ")", params);
        return id;
    }
}
