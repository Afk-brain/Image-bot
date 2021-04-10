package org.mo.searchbot;

import org.mo.searchbot.utils.BotCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class CommandBot extends TelegramLongPollingBot {

    private Map<Pattern, Method> methods;
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public CommandBot() {
        methods = new HashMap<>();
        Class<?> botClass = getClass();
        for(Method method : botClass.getMethods()) {
            for(Annotation annotation : method.getDeclaredAnnotations()) {
                if(annotation instanceof BotCommand) {
                    Pattern pattern = Pattern.compile(((BotCommand) annotation).value());
                    methods.put(pattern, method);
                    log.info("{} associated with {}", pattern, method.getName());
                }
            }
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            String input = update.getMessage().getText();
            User user = update.getMessage().getFrom();
            log.info("{}: {}", user.getFirstName() + user.getLastName() + user.getUserName(), input);
            methods.forEach(((pattern, method) -> {
                if(pattern.matcher(input).matches()) {
                    try {
                        log.info("Input:\"{}\" invoke method:{}",input,method.getName());
                        method.invoke(this,update);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }));
        }
    }
}
