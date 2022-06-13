package org.example.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.example.Database;

import java.util.Arrays;

public class setup extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent e){
        String args[] = e.getMessage().getContentRaw().split(" ");
        String serverId = e.getGuild().getId();
        if(args[0].equalsIgnoreCase("sc")){
            switch (args[1]){
                case "help":
                    e.getMessage().reply("**scavenger hunt help commands:** \n" +
                            " \n" +
                            "`sc set question` **it will start the mission chain, this should be your first mission with clues** \n" +
                            "\n" +
                            "`sc set answer-response` **it will add missions (to maintain a chain, set your answer to previous questions answer)** \n" +
                            "Example: `sc set 4-you got it right` \n" +
                            "\n" +
                            "`sc start` **it will start the scavenger hunt**\n" +
                            "\n" +
                            "`sc clear` **it will clear every mission**").queue();
                    break;

                case "set":
                    if(args[2].equalsIgnoreCase("question")){
                        e.getChannel().sendMessage("**Starter question set!**").queue();
                        StringBuilder question = new StringBuilder();
                        for(int i = 3; i < args.length; i++){
                            question.append(args[i] + " ");
                        }
                        Database.updateDB(serverId, "sc start", question.toString());
                        Database.set(serverId, "triggers", "sc start-");
                    }



            }
        }
        if(args.length >= 3){
            if(args[2].equalsIgnoreCase("set!**")){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                e.getMessage().delete().queue();
            }}
    }
}
