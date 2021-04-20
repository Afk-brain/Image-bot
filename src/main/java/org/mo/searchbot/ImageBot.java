package org.mo.searchbot;

import org.mo.searchbot.utils.BotCommand;
import org.mo.searchbot.utils.GoogleImageService;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.IOException;
import java.util.*;

public class ImageBot extends CommandBot {

    private final GoogleImageService imageService = new GoogleImageService();
    private Map<Long, List<String>> storage = new HashMap<>();

    public ImageBot() throws IOException {}

    @BotCommand("!.+")
    public void sendPhoto(Message message) {
        String query = message.getText().substring(1);
        List<String> links = imageService.getImages(query);
        SendPhoto sendPhoto = SendPhoto.builder()
                .chatId(message.getChatId().toString())
                .photo(new InputFile(links.get(0)))
                .replyToMessageId(message.getMessageId())
                .replyMarkup(getKeyboard(links, 0))
                .build();
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardMarkup getKeyboard(List<String> links, int pos) {
        long id = System.currentTimeMillis();
        storage.put(id, links);
        return getKeyboard(links, pos, id);
    }

    private InlineKeyboardMarkup getKeyboard(List<String> links, int pos, long id) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        if(pos - 1 >= 0) {
            row.add(InlineKeyboardButton.builder().text((pos - 1) + "<<<").callbackData(id + " " + (pos - 1)).build());
        }
        if(pos + 1 < links.size()) {
            row.add(InlineKeyboardButton.builder().text(">>>" + (pos + 1)).callbackData(id + " " + (pos + 1)).build());
        }
        return InlineKeyboardMarkup.builder().keyboardRow(row).build();
    }

    @Override
    protected void processCallbackQuery(CallbackQuery query) {
        String data = query.getData();
        String[] parts = data.split(" ");
        long id = Long.parseLong(parts[0]);
        int pos = Integer.parseInt(parts[1]);
        List<String> links = storage.get(id);
        EditMessageMedia media = EditMessageMedia.builder()
                .messageId(query.getMessage().getMessageId())
                .chatId(query.getMessage().getChatId().toString())
                .replyMarkup(getKeyboard(links, pos, id))
                .media(InputMediaPhoto.builder().media(links.get(pos)).build())
                .build();
        try {
            execute(media);
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
