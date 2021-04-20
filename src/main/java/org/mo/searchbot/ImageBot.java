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
    private Map<Integer, List<String>> storage = new HashMap<>();
    private int id = 0;

    public ImageBot() throws IOException {}

    @BotCommand("!.+")
    public void sendPhoto(Message message) {
        String query = message.getText().substring(1);
        List<String> links = imageService.getImages(query);
        SendPhoto sendPhoto = SendPhoto.builder()
                .chatId(message.getChatId().toString())
                .photo(new InputFile(links.get(0)))
                .replyToMessageId(message.getMessageId())
                .replyMarkup(getKeyboard(links, 0, 0))
                .build();
        try {
            execute(sendPhoto);
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

    private InlineKeyboardMarkup getKeyboard(List<String> links, int pos, int oldId) {
        int id = oldId;
        if(id == 0) {
            id = this.id++;
            storage.put(id, links);
        }
        List<InlineKeyboardButton> row = new ArrayList<>();
        if(pos - 1 >= 0) {
            row.add(InlineKeyboardButton.builder().text((pos - 1) + "<<<").callbackData(id + "-" + pos).build());
        }
        if(pos + 1 < links.size()) {
            row.add(InlineKeyboardButton.builder().text(">>>" + (pos + 1)).callbackData(id + "+" + pos).build());
        }
        return InlineKeyboardMarkup.builder().keyboardRow(row).build();
    }

    @Override
    protected void processCallbackQuery(CallbackQuery query) {
        String data = query.getData();
        String direction = data.contains("-") ? "-" : "\\+";
        int id = Integer.parseInt(data.split(direction)[0]);
        int pos = Integer.parseInt(data.split(direction)[1]);
        pos = direction.equals("-") ? --pos : ++pos;
        List<String> links = storage.get(id);
        answerCallbackQuery(query, getKeyboard(links, pos, id),links.get(pos));
    }

    private void answerCallbackQuery(CallbackQuery query, InlineKeyboardMarkup keyboard, String link) {
        EditMessageMedia media = EditMessageMedia.builder()
                .messageId(query.getMessage().getMessageId())
                .chatId(query.getMessage().getChatId() + "")
                .replyMarkup(keyboard)
                .media(InputMediaPhoto.builder().media(link).build())
                .build();
        try {
            execute(media);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
