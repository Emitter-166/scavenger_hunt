package org.example;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.example.listeners.respond;
import org.example.listeners.setup;
import org.example.listeners.starter;

import javax.security.auth.login.LoginException;

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