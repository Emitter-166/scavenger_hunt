package org.example.listeners;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.example.Database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.example.Database.collection;

public class respond extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent e){
        User author = e.getAuthor();
        if(author.isBot()) return;

        String guildId = e.getGuild().getId();
        Document serverDoc = null;
        try{
             serverDoc = (Document) collection.find(new Document("serverId", guildId)).cursor().next();
        }catch (NoSuchElementException exception){
            Database.createDB(e.getGuild().getId());
        }
        List<String> triggers = new ArrayList<>();
        try{
            Arrays.stream(((String)(serverDoc).get("triggers")).split("-")).forEach(trigger -> triggers.add(trigger));
        }catch(Exception ex){}

        if((Arrays.stream(triggers.toArray()).anyMatch(trigger -> e.getMessage().getContentRaw().equalsIgnoreCase((String) trigger)))){
            String response = ((String)(serverDoc).get(e.getMessage().getContentRaw()));
            e.getChannel().sendMessage(response).queue();
            //here should be leaderboard embed

        }

    }
}
