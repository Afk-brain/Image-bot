package org.mo.searchbot;

import org.mo.searchbot.utils.BotCommand;
import org.mo.searchbot.utils.GoogleImageService;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class ImageBot extends CommandBot {

    private final GoogleImageService imageService = new GoogleImageService();

    public ImageBot() throws IOException {}

    @BotCommand("!.+")
    public void sendPhoto(Update update) {
        String query = update.getMessage().getText().substring(1);
        List<String> links = imageService.getImages(query);
        SendPhoto photo = SendPhoto.builder()
                .chatId(update.getMessage().getChatId().toString())
                .photo(new InputFile(randomLink(links)))
                .replyToMessageId(update.getMessage().getMessageId())
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

    @Override
    public String getBotUsername() {
        return System.getenv("TelegramBotUsername");
    }

    @Override
    public String getBotToken() {
        return System.getenv("TelegramBotToken");
    }
}
