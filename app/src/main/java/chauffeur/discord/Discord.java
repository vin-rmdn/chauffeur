package chauffeur.discord;

import java.sql.SQLException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import chauffeur.discord.subscriber.SubscribeService;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.MessageReferenceData;

@Component
public class Discord {
    SubscribeService service;
    String token;
    long ownId;

    final Logger logger = LoggerFactory.getLogger(Discord.class);

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
        MessageReferenceData reference = MessageReferenceData.builder().messageId(message.getId().asLong()).build();

        switch (content) {
            case "hi":
                channel.createMessage("https://nohello.net").withMessageReference(reference).block();
                break;
            case "!subscribe":
                try {
                    service.subscribe(user.get().getId());
                } catch (SQLException e) {
                    channel.createMessage("Failed to subscribe: " + e.getMessage())
                            .withMessageReference(reference).block();

                    return;
                }

                channel.createMessage("Subscribed!").withMessageReference(reference).block();
                break;

            // Add more commands here

            default:
                channel.createMessage("Lo siento, no entiendo ese comando.").withMessageReference(reference).block();
                break;
        }
    }

    public void inform(ReadyEvent event) {
        User workerUser = event.getSelf();

        logger.atInfo().addKeyValue("gateway_version", event.getGatewayVersion())
                .addKeyValue("username", workerUser.getUsername())
                .addKeyValue("id", workerUser.getId()).addKeyValue("session_id", event.getSessionId())
                .log("Worker is ready to receive Discord events");
    }

    public void startWorker() {
        DiscordClient client = DiscordClient.create(token);
        GatewayDiscordClient gateway = client.login().block();

        ownId = client.getSelf().block().id().asLong();

        gateway.on(ReadyEvent.class).subscribe(event -> {
            inform(event);
        });

        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            handleMessageCreateEvent(event);
        });

        gateway.onDisconnect().block();
    }
}
