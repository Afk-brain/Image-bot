package org.mo.searchbot;

import org.mo.searchbot.utils.BotCommand;
import org.mo.searchbot.utils.GoogleImageService;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
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
        User user = message.getFrom();
        log.info("Sending {} to \"{} {}\", for \"{}\"",links.get(0), user.getFirstName(), user.getUserName(), query);
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardMarkup getKeyboard(List<String> links, int pos) {
        long id = System.nanoTime();
        storage.put(id, links);
        log.info("Creating id: {}", id);
        return getKeyboard(links, pos, id);
    }

    private InlineKeyboardMarkup getKeyboard(List<String> links, int pos, long id) {
        log.info("Creating keyboard for {} position and {} id from {} images", pos, id, links.size());
        List<InlineKeyboardButton> row = new ArrayList<>();
        if(pos - 1 >= 0) {
            row.add(InlineKeyboardButton.builder().text("◀").callbackData(id + " " + (pos - 1) + " " + System.nanoTime()).build());
        }
        row.add(InlineKeyboardButton.builder().text(pos + 1 + "").callbackData("-2").build());
        if(pos + 1 < links.size()) {
            row.add(InlineKeyboardButton.builder().text("▶" ).callbackData(id + " " + (pos + 1) + " " + System.nanoTime()).build());
        }
        return InlineKeyboardMarkup.builder().keyboardRow(row).build();
    }

    @Override
    protected void processCallbackQuery(CallbackQuery query) {
        log.info("Processing query");
        String data = query.getData();
        if(data.startsWith("-")) {
            sendNotification(query, "?", false);//TODO change notification text
            return;
        }
        String[] parts = data.split(" ");
        long id = Long.parseLong(parts[0]);
        int pos = Integer.parseInt(parts[1]);
        List<String> links = storage.get(id);
        if(links == null) {
            sendNotification(query, "Image timeout", true);
            return;
        }
        EditMessageMedia media = EditMessageMedia.builder()
                .messageId(query.getMessage().getMessageId())
                .chatId(query.getMessage().getChatId().toString())
                .replyMarkup(getKeyboard(links, pos, id))
                .media(InputMediaPhoto.builder().media(links.get(pos)).build())
                .build();
        while(true) {
            try {
                log.info("Sending {} to \"{} {}\"",media.getMedia().getMedia(), query.getFrom().getFirstName(), query.getFrom().getUserName());
                execute(media);
                break;
            } catch (TelegramApiException e) {
                e.printStackTrace();
                log.info("Removing {}, now links contains {} images", links.remove(pos), links.size());
                media.setReplyMarkup(getKeyboard(links, pos, id));
                media.setMedia(InputMediaPhoto.builder().media(links.get(pos)).build());
            }
        }
    }

    private boolean sendNotification(CallbackQuery query, String text, boolean notification) {
        log.info("Sending notification to \"{} {}\" with text \"{}\"", query.getFrom().getFirstName(), query.getFrom().getUserName(), text);
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .text(text)
                .callbackQueryId(query.getId())
                .showAlert(notification)
                .build();
        try {
            execute(answer);
            return true;
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return false;
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
