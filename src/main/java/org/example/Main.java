package org.example;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.example.listeners.respond;
import org.example.listeners.setup;
import org.example.listeners.starter;

import javax.security.auth.login.LoginException;
/*
How this programme works?
First we have:
setup class, it:
    deletes unnecessary messages
    sets required values (like responses, triggers etc.) according to users needs, and save them in database

respond class:
    it responds to the message according to provided values on database,
    and keep tracks of points of participants

starter class:
    it responds to sc start command and starts the chain by asking starter question from
    database, it has a separate class because it shares the trigger field of the database and
    is different on implementation on how to use it

Database class:
    it's a very simple mongodb database connection with some simple methods

*/
public class Main {
    public static void main(String[] args) throws LoginException {
        JDA jda = JDABuilder.createLight(System.getenv("token"))
                .addEventListeners(new respond())
                .addEventListeners(new setup())
                .addEventListeners(new Database())
                .addEventListeners(new starter())
                .setActivity(Activity.listening("sc help"))
                .build();
    }
}