package chauffeur.discord.subscriber;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Repository {
    Connection conn;
    final Logger logger = LoggerFactory.getLogger(Repository.class);

    public Repository(Connection conn) {
        this.conn = conn;
    }

    public void save(long id) {
        final String query = "INSERT INTO subscribers (id) VALUES (?)";

        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setLong(1, id);

            logger.atDebug().addKeyValue("statement", statement).log("SQL statement is prepared");

            int updatedRows = statement.executeUpdate();
            logger.atDebug().addKeyValue("updated_rows", updatedRows).log("SQL statement is executed");
        } catch (SQLException e) {
            logger.error("Failed to save subscriber", e);
        }
    }
}
