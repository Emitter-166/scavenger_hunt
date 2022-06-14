package org.example.listeners;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.example.Database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class respond extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent e){
        User author = e.getAuthor();
        if(author.isBot()) return;
        if(e.getMessage().getContentRaw().equalsIgnoreCase("sc start")) return;


        String guildId = e.getGuild().getId();
        Document serverDoc = null;
        try{
             serverDoc = Database.get(guildId);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        List<String> triggers = new ArrayList<>();
        try{
            Arrays.stream(((String)(serverDoc).get("triggers")).split("-")).forEach(trigger -> triggers.add(trigger));
        }catch(Exception ex){}

        if((Arrays.stream(triggers.toArray()).anyMatch(trigger -> e.getMessage().getContentRaw().equalsIgnoreCase((String) trigger)))){
            e.getChannel().sendMessage(String.format("**%s got the correct answer! next clue is send in your dms :)**", author.getAsMention())).queue();
            e.getMessage().delete().queue();
            String response = ((String)(serverDoc).get(e.getMessage().getContentRaw().toLowerCase()));
            try{
                author.openPrivateChannel().flatMap(channel -> channel.sendMessage(response)).queue();
            }catch (Exception exception){}

            //here should be leaderboard embed

        }

    }
}
