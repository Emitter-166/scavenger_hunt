package org.example.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.example.Database;

import java.util.*;
import java.util.stream.Collectors;


public class respond extends ListenerAdapter {
    Map<String, Float> leaderBoard = new HashMap<>();
    List<Float> salt = new ArrayList<>(); //this will help differenciate similar values while sorting we are basically keep track of it here

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
            try{
              String answered[] =  Database.get(guildId).get(author.getId()).toString().split("-");
              if(!(Arrays.stream(answered).anyMatch(answer -> answer.equalsIgnoreCase(e.getMessage().getContentRaw().toLowerCase())))) {
                  e.getChannel().sendMessage(String.format("**%s got the correct answer! next clue is send in your dms :)**", author.getAsMention())).queue();
                  String response = ((String)(serverDoc).get(e.getMessage().getContentRaw().toLowerCase()));
                  try{
                      author.openPrivateChannel().flatMap(channel -> channel.sendMessage(response)).queue();
                  }catch (Exception exception){}
                  leaderBoard.put(author.getId() , leaderBoard.get(author.getId()) + 1);
                  Database.addUpdate(guildId, author.getId(),e.getMessage().getContentRaw().toLowerCase()+"-");
              }else{
                  e.getChannel().sendMessage(String.format(" %s You've already completed that mission", author.getAsMention())).queue();
              }

            }catch (Exception exception){
                Database.addUpdate(guildId, author.getId(),e.getMessage().getContentRaw().toLowerCase()+"-");
                leaderBoard.put(author.getId() , 1f);
            }
            e.getMessage().delete().queue();
            Calendar calendar = new GregorianCalendar();
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Game Leaderboard:");
            builder.setDescription(LeaderBoardSort(leaderBoard, e.getGuild()));
            builder.setFooter(String.format("%s/%s/%s", calendar.get(Calendar.DAY_OF_MONTH),  calendar.get(Calendar.MONTH),  calendar.get(Calendar.YEAR)));
            e.getChannel().sendMessageEmbeds(builder.build()).queue();
        }

    }

    public String LeaderBoardSort(Map leaderBoard, Guild guild){
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        Map<Float, String> realIndexes = new HashMap<>();

        String names[] = new String[leaderBoard.keySet().size()];
        leaderBoard.keySet().toArray(names);

        List<Float> scores = new ArrayList<>(); Arrays.stream(names).forEach(name -> scores.add((Float) leaderBoard.get(name) + random.nextFloat()));

        for(int i = 0; i < scores.size(); i++){
            realIndexes.put(scores.get(i), names[i]);
        }
        List<Float> sortedScores = scores.stream().sorted().collect(Collectors.toList());
        for(int i = sortedScores.size() - 1; i+1 > 0; i--){
            builder.append(String.format("%s. %s - %s points \n", i + 1, guild.retrieveMemberById(realIndexes.get(sortedScores.get(i))).complete().getAsMention(), Math.floor(sortedScores.get(i))));
        }

        return builder.toString();
    }
}
