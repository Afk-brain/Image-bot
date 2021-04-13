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
import java.util.Random;

public class ImageBot extends CommandBot {

    private final GoogleImageService imageService = new GoogleImageService();

    public ImageBot() throws IOException {}

    @BotCommand("!.+")
    public void sendPhoto(Message message) {
        String query = message.getText().substring(1);
        List<String> links = imageService.getImages(query);
        SendPhoto photo = SendPhoto.builder()
                .chatId(message.getChatId().toString())
                .photo(new InputFile(randomLink(links)))
                .replyToMessageId(message.getMessageId())
                .build();
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String randomLink(List<String> links) {
        Random random = new Random();
        return links.get(random.nextInt(Math.min(9, links.size())));
    }

    @BotCommand("\\/help")
    public void help(Message message) {
        replyText(message, "Big Floppa is gay");
    }

    @BotCommand("\\/version")
    public void version(Message message) {
        replyText(message, "Alpha_v1.0");
    }

    private void replyText(Message message, String text) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(message.getChatId() + "")
                .text(text)
                .replyToMessageId(message.getMessageId()).build();
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
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
