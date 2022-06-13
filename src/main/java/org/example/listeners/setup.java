package org.example.listeners;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.example.Database;

public class setup extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent e){
        if(e.getChannel().getType().equals(ChannelType.PRIVATE)) return;
        if(!(e.getMember().hasPermission(Permission.MODERATE_MEMBERS))) return;

        String args[] = e.getMessage().getContentRaw().split(" ");
        String serverId = e.getGuild().getId();

        if(args.length == 3){
            if(args[2].equalsIgnoreCase("set!**") || args[2].equalsIgnoreCase("cleared!**") ){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                e.getMessage().delete().queue();
            }
        } else if (args.length == 4) {
            if(args[3].equalsIgnoreCase("set!**")){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                e.getMessage().delete().queue();
            }
        }

        if(e.getAuthor().isBot()) return;


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
                    e.getMessage().delete().queue();
                    break;

                case "set":
                    if(args[2].equalsIgnoreCase("question")){
                        e.getChannel().sendMessage("**Starter question set!**").queue();
                        StringBuilder question = new StringBuilder();
                        for(int i = 3; i < args.length; i++){
                            question.append(args[i] + " ");
                        }
                        Database.set(serverId, "sc start", question.toString());
                        Database.addUpdate(serverId, "triggers", "sc start-");
                    }else{
                        //answer-response
                        e.getChannel().sendMessage("**answer + response set!**").queue();
                        StringBuilder together = new StringBuilder();

                        for(int i = 2; i < args.length; i++){
                            together.append(args[i] + " ");
                        }
                        String specialArgs[] = together.toString().split("-");
                        String answer = specialArgs[0];
                        String response = specialArgs[1];
                        Database.set(serverId, answer, response);
                        Database.addUpdate(serverId, "triggers", answer + "-");
                    }
                    e.getMessage().delete().queue();

                    break;

                case "clear":
                    e.getChannel().sendMessage("**Scavenger hunt cleared!**").queue();
                    Database.collection.deleteOne(new Document("serverId", serverId));
                    e.getMessage().delete().queue();
                    break;

            }
        }
    }
}
