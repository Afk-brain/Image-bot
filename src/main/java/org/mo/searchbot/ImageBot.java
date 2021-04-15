package org.mo.searchbot;

import org.mo.searchbot.utils.BotCommand;
import org.mo.searchbot.utils.GoogleImageService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class ImageBot extends CommandBot {

    private final GoogleImageService imageService = new GoogleImageService();
    private final static int ATTEMPTS = 5;

    public ImageBot() throws IOException {}

    @BotCommand("!.+")
    public void sendPhoto(Message message) {
        String query = message.getText().substring(1);
        List<String> links = imageService.getImages(query);
        SendPhoto sendPhoto = SendPhoto.builder()
                .chatId(message.getChatId().toString())
                .photo(new InputFile(chooseLink(links)))
                .replyToMessageId(message.getMessageId())
                .build();
        int c = 0;
        while(c++ < ATTEMPTS) {
            try {
                execute(sendPhoto);
                return;
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            sendPhoto.setPhoto(new InputFile(chooseLink(links)));
        }
        replyText(message, "Fuck off");
    }

    private String chooseLink(List<String> links) {//TODO rework
        Random random = new Random();
        return links.get(random.nextInt(Math.min(9, links.size())));
    }

    @BotCommand("\\/help")
    public void help(Message message) {
        replyText(message, "Big Floppa is gay");//TODO add description
    }

    @BotCommand("\\/version")
    public void version(Message message) {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/gis.prefs"));
            replyText(message, properties.getProperty("version"));
        } catch (IOException e) {
            e.printStackTrace();
            replyText(message, "Nice dick, bro");
        }
    }

    @Override
    public String getBotUsername() {
        return System.getenv("TelegramBotUsername");
    }

    @Override
    public String getBotToken() {
        return System.getenv("TelegramBotToken");
    }
}
