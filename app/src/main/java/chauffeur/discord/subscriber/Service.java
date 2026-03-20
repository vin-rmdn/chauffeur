package chauffeur.discord.subscriber;

public class Service {
    Repository repository;

    public Service (Repository repository) {
        this.repository = repository;
    }

    void subscribe(long id) {
        repository.save(id);
    }
}
