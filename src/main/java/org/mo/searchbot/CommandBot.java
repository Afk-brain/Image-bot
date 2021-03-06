package org.mo.searchbot;

import org.mo.searchbot.utils.BotCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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
            if(input.contains("@" + getBotUsername())) {
                input = input.replaceAll("@" + getBotUsername(), "");
            }
            String text = input;
            User user = update.getMessage().getFrom();
            log.info("Username: {}; Tag: {}; Input: {};", user.getFirstName(), user.getUserName(), input);
            methods.forEach(((pattern, method) -> {
                if(pattern.matcher(text).matches()) {
                    try {
                        log.info("Invoking method:\"{}\", for input:\"{}\"", method.getName(),text);
                        method.invoke(this,update.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }));
        } else if(update.hasCallbackQuery()) {
            log.info("Calling processCallbackQuery() for \"{} {}\", query data: {}", update.getCallbackQuery().getFrom().getFirstName()
                    , update.getCallbackQuery().getFrom().getUserName(), update.getCallbackQuery().getData());
            processCallbackQuery(update.getCallbackQuery());
        }
    }

    protected void replyText(Message message, String text) {
        log.info("Replying to \"{} {}\"her with \"{}\"",message.getFrom().getFirstName(), message.getFrom().getUserName(), text);
        SendMessage sendMessage = SendMessage.builder()
                .chatId(message.getChatId() + "")
                .text(text)
                .parseMode("MarkdownV2")
                .replyToMessageId(message.getMessageId()).build();
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    protected abstract void processCallbackQuery(CallbackQuery callbackQuery);
}
