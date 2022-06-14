package org.example.listeners;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.example.Database;

public class starter extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent e){
        if(e.getChannel().getType().equals(ChannelType.PRIVATE)) return;
        //checks if it's in dms
        try {
            if (!(e.getMember().hasPermission(Permission.MODERATE_MEMBERS))) return;
            //checks if the member is a moderator
        }catch (Exception exception){}

        if(e.getMessage().getContentRaw().equalsIgnoreCase("sc start")) {
            e.getMessage().delete().queue();
            Document serverDoc;
            try{
                serverDoc = Database.get(e.getGuild().getId());
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            String response = ((String)(serverDoc).get(e.getMessage().getContentRaw()));
            e.getChannel().sendMessage(String.format("**%s**", response)).queue(); //sends the started question

        }

    }
}
