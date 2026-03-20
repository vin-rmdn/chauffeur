package chauffeur.discord.subscriber;

import discord4j.common.util.Snowflake;

public class Service {
    Repository repository;

    public Service (Repository repository) {
        this.repository = repository;
    }

    public void subscribe(Snowflake id) {
        repository.save(id.asLong());
    }
}
