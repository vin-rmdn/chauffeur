package chauffeur.discord;

import java.sql.SQLException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import chauffeur.discord.subscriber.SubscribeService;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

@Component
public class Discord {
    SubscribeService service;
    String token;
    long ownId;

    @Autowired
    public Discord(SubscribeService service,
            @Value("${discord.token}") String token) {
        this.service = service;
        this.token = token;
    }

    public Discord(SubscribeService service, String token, long ownId) {
        this.service = service;
        this.token = token;
        this.ownId = ownId;
    }

    public void handleMessageCreateEvent(MessageCreateEvent event) {
        Message message = event.getMessage();
        MessageChannel channel = message.getChannel().block();

        Optional<User> user = message.getAuthor();
        if (user.isEmpty()) {
            channel.createMessage("Error: no author is found").block();

            return;
        }

        if (user.get().getId().asLong() == ownId) {
            return;
        }

        String content = message.getContent();
        switch (content) {
            case "hi":
                channel.createMessage("https://nohello.net").block();
                break;
            case "!subscribe":
                try {
                    service.subscribe(user.get().getId());
                } catch (SQLException e) {
                    channel.createMessage("Failed to subscribe: " + e.getMessage()).block();

                    return;
                }

                channel.createMessage("Subscribed!").block();
                break;

            // Add more commands here

            default:
                channel.createMessage("Lo siento, no entiendo ese comando.").block();
                break;
        }
    }

    public void startWorker() {
        DiscordClient client = DiscordClient.create(token);
        GatewayDiscordClient gateway = client.login().block();

        ownId = client.getSelf().block().id().asLong();

        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            handleMessageCreateEvent(event);
        });

        gateway.onDisconnect().block();
    }
}
