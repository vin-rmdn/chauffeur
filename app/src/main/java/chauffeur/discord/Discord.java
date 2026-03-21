package chauffeur.discord;

import java.sql.SQLException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import chauffeur.discord.subscriber.SubscribeService;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.core.spec.MessageCreateMono;
import discord4j.discordjson.json.MessageReferenceData;

@SpringBootApplication
public class Discord implements CommandLineRunner {
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
        logger.atInfo().addKeyValue("content", content).addKeyValue("user", user.get().getUsername())
                .log("receiving message");

        ThreadChannel thread;
        if (channel instanceof ThreadChannel)
            thread = (ThreadChannel) channel;
        else
            thread = message.createPublicThread(String.format("%s (%s)", content,
                    message.getTimestamp().toString()))
                    .block();
        logger.atInfo().addKeyValue("thread_id", thread.getId().asLong()).log("Thread created");

        switch (content) {
            case "hi":
                reply(message, thread, "https://nohello.net");

                break;
            case "!subscribe":
                try {
                    service.subscribe(user.get().getId());
                } catch (SQLException e) {
                    reply(message, thread, "Failed to subscribe: %s".formatted(e.getMessage()));

                    return;
                }

                reply(message, thread, "Subscribed!");
                break;

            // Add more commands here

            default:
                reply(message, thread, "Lo siento, no entiendo ese comando.");

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

    void reply(Message original, ThreadChannel thread, String content) {
        boolean insideThread = (original.getChannel().block() instanceof ThreadChannel);

        MessageCreateMono msg = thread.createMessage(content);
        if (insideThread) {
            MessageReferenceData reference = MessageReferenceData.builder().messageId(original.getId().asLong())
                    .build();

            msg = msg.withMessageReference(reference);
        }

        msg.block();
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

    @Override
    public void run(String... args) throws Exception {
        startWorker();
    }
}
