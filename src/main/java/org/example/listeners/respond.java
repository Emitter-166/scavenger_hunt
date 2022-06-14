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
    public static Map<String, Float> leaderBoard = new HashMap<>(); //this will keep track of our players and their points
    public static List<Float> salt = new ArrayList<>(); //this will help differentiate similar values while sorting we are basically keep track of it here

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        User author = e.getAuthor();
        if (author.isBot()) return;
        if (e.getMessage().getContentRaw().equalsIgnoreCase("sc start")) return;
        //we have another class for starting the game for reasons, so we just break out of the programme here

        String guildId = e.getGuild().getId();
        Document serverDoc = null;
        try {
            //retrieving data from database
            serverDoc = Database.get(guildId);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        List<String> triggers = new ArrayList<>();
        try {
            //it will retrieve the triggers (aka messages to respond to) from the database (it has been set in setup class
            Arrays.stream(((String) (serverDoc).get("triggers")).split("-")).forEach(trigger -> triggers.add(trigger));
        } catch (Exception ex) {
        }

        if ((Arrays.stream(triggers.toArray()).anyMatch(trigger -> e.getMessage().getContentRaw().equalsIgnoreCase((String) trigger)))) {
            //checking if the received message matches one of the triggers set by the host

            String[] answered = null;
            try {
                //it will check the participant has already answered that question
                answered = Database.get(guildId).get(author.getId()).toString().split("-");

            } catch (Exception exception) {
                //if there is no data about the participant on database, it will create one.
                System.out.println("No database for that user, creating...");
                try {
                    Database.addUpdate(guildId, author.getId(), "");
                    leaderBoard.put(author.getId(), 0f);
                    answered = Database.get(guildId).get(author.getId()).toString().split("-");
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }

            if (!(Arrays.stream(answered).anyMatch(answer -> answer.equalsIgnoreCase(e.getMessage().getContentRaw().toLowerCase())))) {
                //checks if the person has already answered the question
                e.getChannel().sendMessage(String.format("**%s got the correct answer! next clue is send in your dms :)**", author.getAsMention())).queue();
                String response = ((String) (serverDoc).get(e.getMessage().getContentRaw().toLowerCase()));
                author.openPrivateChannel().flatMap(channel -> channel.sendMessage(response)).queue(); //sending response to the persons dms
                leaderBoard.put(author.getId(), leaderBoard.get(author.getId()) + 1f); //updating leaderboard for that user
                Database.addUpdate(guildId, author.getId(), e.getMessage().getContentRaw().toLowerCase() + "-");

            } else {
                e.getChannel().sendMessage(String.format(" %s You've already completed that mission", author.getAsMention())).queue();
            }

            e.getMessage().delete().queue();

            //sending the leaderboard
            Calendar calendar = new GregorianCalendar();
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Game Leaderboard:");
            builder.setDescription(LeaderBoardSort(leaderBoard, e.getGuild()));
            builder.setFooter(String.format("%s/%s/%s", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)));
            e.getChannel().sendMessageEmbeds(builder.build()).queue();
        }

    }

    public String LeaderBoardSort(Map leaderBoard, Guild guild) {
        /*in this method we build the leaderboard according to values from leaderboard map shown earlier.
        so it first takes the values and keys from the map, sorts out the values, and adds the value back
        with the corresponding key (aka participant). then with the help of a stringbuilder, it builds the
        leaderboard description
        */

        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        Map<Float, String> realIndexes = new HashMap<>();

        String[] names = new String[leaderBoard.keySet().size()];
        leaderBoard.keySet().toArray(names);

        List<Float> scores = new ArrayList<>();
        float randomSalt = 0;
        float finalRandomSalt = randomSalt;
        do {
            randomSalt = random.nextFloat();
        }while (Arrays.stream(salt.toArray()).anyMatch(usedSalts -> usedSalts.equals(finalRandomSalt)));
        float finalRandomSalt1 = randomSalt;
        /*since we will not have the keys, in order to identify which value is for which key, we need something
        that will save the old keys and values, so we can get the key back after sorting. we are salting
        the values in case we have the same values twice or more. if we don't do it, it will be impossible
        for us to find the right value for the key incase there is multiple similar values
         */
        Arrays.stream(names).forEach(name -> scores.add((Float) leaderBoard.get(name) + finalRandomSalt1));
        for (int i = 0; i < scores.size(); i++) {
            realIndexes.put(scores.get(i), names[i]);
        }
        //salting and keeping track of indexes ends here


        List<Float> sortedScores = scores.stream().sorted().collect(Collectors.toList());
        for (int i = sortedScores.size() - 1, j = 0 ; i + 1 > 0; i--, j++) {
            //here we sort it with ascending order and reverse the for loop to make it descending lol, we also find the key and filter out the added salt here
            builder.append(String.format("%s. %s - %s points \n", j + 1, guild.retrieveMemberById(realIndexes.get(sortedScores.get(i))).complete().getAsMention(), Math.floor(sortedScores.get(i))));
        }

        return builder.toString();
    }
}
