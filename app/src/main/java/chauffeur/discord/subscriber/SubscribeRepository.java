package chauffeur.discord.subscriber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SubscribeRepository {
    final Logger logger = LoggerFactory.getLogger(SubscribeRepository.class);

    JdbcTemplate template;

    @Autowired
    public SubscribeRepository(JdbcTemplate template) {
        this.template = template;
    }

    public void save(long id) throws DataAccessException {
        final String query = "INSERT INTO subscribers (user_id) VALUES (?);";

        try {
            int updatedRows = template.update(query, id);

            logger.atDebug().addKeyValue("updated_rows", updatedRows).log("SQL statement is executed");
        } catch (DataAccessException e) {
            logger.error("Failed to save subscriber", e);

            throw e;
        }
    }
}
