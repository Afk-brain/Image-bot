package org.mo.searchbot;

import org.mo.searchbot.sevrices.GoogleImageService;
import org.mo.searchbot.sevrices.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.IOException;
import java.util.List;

public class ImageBot extends TelegramLongPollingBot {

    private Logger log = LoggerFactory.getLogger(ImageBot.class);
    private ImageService imageService = new GoogleImageService();

    public ImageBot() throws IOException {}

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().startsWith("!")) {
            sendPhoto(update);
        }
    }

    private void sendPhoto(Update update) {
        String query = update.getMessage().getText().substring(1);
        log.info("Message: {}\nFrom: {}", query, update.getMessage().getFrom().toString());
        List<String> links = imageService.getImages(query);
        SendPhoto photo = SendPhoto.builder()
                .chatId(update.getMessage().getChatId() + "")
                .photo(new InputFile(links.get(0)))
                .build();
        try {
            execute(photo);
            log.info("Answer for {}: {}\nAll links: {}", update.getMessage().getFrom().toString(), links.get(0), links);
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
