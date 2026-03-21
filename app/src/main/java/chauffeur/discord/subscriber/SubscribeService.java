package chauffeur.discord.subscriber;

import discord4j.common.util.Snowflake;
import java.sql.SQLException;
import org.springframework.stereotype.Service;

@Service
public class SubscribeService {
  SubscribeRepository repository;

  public SubscribeService(SubscribeRepository repository) {
    this.repository = repository;
  }

  public void subscribe(Snowflake id) throws SQLException {
    repository.save(id.asLong());
  }
}
