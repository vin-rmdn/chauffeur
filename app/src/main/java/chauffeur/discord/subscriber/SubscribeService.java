package chauffeur.discord.subscriber;

import java.sql.SQLException;

import discord4j.common.util.Snowflake;

public class SubscribeService {
    SubscribeRepository repository;

    public SubscribeService(SubscribeRepository repository) {
        this.repository = repository;
    }

    public void subscribe(Snowflake id) throws SQLException {
        repository.save(id.asLong());
    }
}
