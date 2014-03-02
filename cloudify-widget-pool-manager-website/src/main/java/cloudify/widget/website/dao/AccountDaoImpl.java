package cloudify.widget.website.dao;

import cloudify.widget.website.models.AccountModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * User: evgenyf
 * Date: 2/27/14
 */
public class AccountDaoImpl implements IAccountDao {

    private static final String TABLE_NAME = "account";
    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {


        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long createAccount( AccountModel account ) {

        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert( jdbcTemplate ).
                                    withTableName(TABLE_NAME).
                                    usingGeneratedKeyColumns("id");

        Map<String,Object> parametersMap = new HashMap<String,Object>(1);
        parametersMap.put( "uuid", account.getUuid() );

        Number id = jdbcInsert.executeAndReturnKey(parametersMap);

        return ( Long )id;
    }

    @Override
    public boolean deleteAccount( Long id ) {
        String delQuery = "delete from " + TABLE_NAME + " where id = ?";
        int count = jdbcTemplate.update(delQuery, new Object[]{id});
        return count > 0;
    }
}