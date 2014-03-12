package cloudify.widget.pool.manager;

import cloudify.widget.pool.manager.dto.NodeModel;
import cloudify.widget.pool.manager.dto.NodeStatus;
import cloudify.widget.pool.manager.dto.PoolStatusCount;
import com.mysql.jdbc.Statement;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * User: eliranm
 * Date: 3/2/14
 * Time: 7:09 PM
 */
public class NodesDao {

    public static final String TABLE_NAME = "nodes";
    public static final String COL_NODE_ID = "id";
    public static final String COL_POOL_ID = "pool_id";
    public static final String COL_NODE_STATUS = "node_status";
    public static final String COL_MACHINE_ID = "machine_id";

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public boolean create(final NodeModel nodeModel) {

        // used to hold the auto generated key in the 'id' column
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        int affected = jdbcTemplate.update(
                new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                        PreparedStatement ps = con.prepareStatement(
                                "insert into " + TABLE_NAME + " (" + COL_POOL_ID + "," + COL_NODE_STATUS + "," + COL_MACHINE_ID + ") values (?, ?, ?)",
                                Statement.RETURN_GENERATED_KEYS // specify to populate the generated key holder
                        );
                        ps.setString(1, nodeModel.poolId);
                        ps.setString(2, nodeModel.nodeStatus.name());
                        ps.setString(3, nodeModel.machineId);
                        return ps;
                    }
                },
                keyHolder
        );

        // keep data integrity - fetch the last insert id and update the model
        nodeModel.id = keyHolder.getKey().longValue();

        return affected > 0;
    }

    public List<PoolStatusCount> getPoolStatusCounts(){
        return jdbcTemplate.query("select count(*) as count, " + COL_POOL_ID + "," + COL_NODE_STATUS + " from " + TABLE_NAME + " group by " + COL_POOL_ID + " , " + COL_NODE_STATUS ,
                new BeanPropertyRowMapper<PoolStatusCount>() );
    }

    public List<NodeModel> readAllOfPool(String poolId) {
        return jdbcTemplate.query("select * from " + TABLE_NAME + " where " + COL_POOL_ID + " = ?",
                new Object[]{poolId},
                new BeanPropertyRowMapper<NodeModel>(NodeModel.class));
    }

    public NodeModel read(long nodeId) {
        try {
            return jdbcTemplate.queryForObject("select * from " + TABLE_NAME + " where " + COL_NODE_ID + " = ?",
                    new Object[]{nodeId},
                    new BeanPropertyRowMapper<NodeModel>(NodeModel.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public int update(NodeModel nodeModel) {
        return jdbcTemplate.update(
                "update " + TABLE_NAME + " set " + COL_POOL_ID + " = ?," + COL_NODE_STATUS + " = ?," + COL_MACHINE_ID + " = ? where " + COL_NODE_ID + " = ?",
                nodeModel.poolId, nodeModel.nodeStatus.name(), nodeModel.machineId, nodeModel.id);
    }

    public int delete(long nodeId) {
        return jdbcTemplate.update("delete from " + TABLE_NAME + " where id = ?", nodeId);
    }





}
