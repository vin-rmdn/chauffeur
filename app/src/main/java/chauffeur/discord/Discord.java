package chauffeur.discord;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;

public class Discord {
    public Discord() {}

    public void handleMessageCreateEvent(MessageCreateEvent event) {
        Message message = event.getMessage();
        String content = message.getContent();

        MessageChannel channel = message.getChannel().block();

        switch (content) {
            case "hi":
                channel.createMessage("https://nohello.net").block();
                break;
            // TODO: add more commands here
            default:
                channel.createMessage("Lo siento, no entiendo ese comando.").block();
                break;
        }
    }
}
