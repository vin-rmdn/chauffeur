package chauffeur.discord;

import java.sql.SQLException;
import java.util.Optional;

import chauffeur.discord.subscriber.Service;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

public class Discord {
    Service service;

    public Discord(Service service) {
        this.service = service;
    }

    public void handleMessageCreateEvent(MessageCreateEvent event) {
        Message message = event.getMessage();
        String content = message.getContent();

        MessageChannel channel = message.getChannel().block();

        switch (content) {
            case "hi":
                channel.createMessage("https://nohello.net").block();
                break;
            case "!subscribe":
                Optional<User> user = message.getAuthor();
                if (user.isEmpty()) {
                    channel.createMessage("Error: no author is found").block();

                    return;
                }

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
}
