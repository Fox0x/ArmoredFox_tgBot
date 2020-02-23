import config.ConfigParser;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

public class Bot extends TelegramLongPollingBot {

    static String authorName;
    static String authorPhoneNumber;
    static int messageID;
    static int authorID;
    static long chatID;
    final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    String messageText;

    public void onUpdateReceived(Update update) {

        authorName = update.getMessage().getFrom().getFirstName();
        messageID = update.getMessage().getMessageId();
        messageText = update.getMessage().getText();
        authorID = update.getMessage().getFrom().getId();
        chatID = update.getMessage().getChatId().intValue();

        DbManager dbManager = new DbManager();

        SendMessage sendMessage = new SendMessage().setChatId(chatID);
        SendSticker sendSticker = new SendSticker().setChatId(chatID);

        ArrayList<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();

        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        //Есть ли сообщение и есть ли в нём текст.
        if (messageText != null) {

            switch (messageText) {
                //Поздоровались.
                case "/start": {
                    //Проверяем на наличие authorID в базе данных.
                    try {
                        dbManager.DbConnection();
                        dbManager.DbExist(authorID);
                        dbManager.connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    //Если authorID нет в БД, то просим номер.
                    if (!dbManager.isUserExists) {
                        try {
                            execute(new SendMessage().setChatId(chatID).enableMarkdown(true).setText(
                                    "Привет! Я бот @ArmoredFox." + "\n" + "\n" +
                                            "Меня создал [Fox_x](tg://user?id=282614062), но пока он не знает для чего." + "\n" +
                                            "Сейчас я умею совсем немного, но постоянно учусь чему-то новому."));
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        //Задержка в пол секунды перед вторым сообщением.
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        sendMessage.setText("Поделись номером");

                        keyboard.clear();
                        keyboardFirstRow.add(new KeyboardButton().setText("Поделится номером \uD83D\uDCF1").setRequestContact(true));
                        keyboardSecondRow.add(new KeyboardButton().setText("Не буду \uD83D\uDD12"));
                        keyboard.add(keyboardFirstRow);
                        keyboard.add(keyboardSecondRow);
                        replyKeyboardMarkup.setKeyboard(keyboard);
                        sendMessage.setReplyMarkup(replyKeyboardMarkup);
                    }
                    //Если authorID есть в БД, то кидаем приветствие и задаём стикеру fileId.
                    else {
                        sendMessage.setText("Привет, " + authorName);
                        sendSticker.setSticker("CAACAgIAAxkBAAIE8V5TBqRu26aF6inNlviQrILWUqAoAAKldwEAAWOLRgw0GESEzOdf4hgE");
                    }
                }
                break;
                //Во сколько на работу.
                case "На сколько мне сегодня":
                case "/time": {
                    Integer dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                    if (dayOfWeek.equals(6) || dayOfWeek.equals(7)) {
                        sendMessage.enableMarkdown(true).setText("Тебе сегодня на [21:00]" +
                                "(https://docs.google.com/spreadsheets/d/1Fh72OSGcpNXXPduv734fC0x5CN3T4qzzJCuAkW6yNp4/)");
                    } else sendMessage.enableMarkdown(true).setText("Тебе сегодня на [20:00]" +
                            "(https://docs.google.com/spreadsheets/d/1Fh72OSGcpNXXPduv734fC0x5CN3T4qzzJCuAkW6yNp4/)");
                }
                break;
                //Погода
                case "/weather": {
                    try {
                        Weather weather = new Weather();
                        sendMessage.setText(weather.WeatherParser());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
                //Твой ID.
                case "/id": {
                    sendMessage.setText(authorName + " ,твой ID: " + authorID).setReplyToMessageId(messageID);
                }
                break;
                //Если не отправил номер.
                case "Не буду \uD83D\uDD12": {
                    sendMessage.setText("Окей \uD83D\uDE14").setReplyMarkup(new ReplyKeyboardRemove().setSelective(true));
                }
                break;
                default:
                    sendMessage.setText("Я не понимаю \uD83D\uDE13");
            }
            try {
                execute(sendMessage);
                if (sendSticker.getSticker() != null) {
                    execute(sendSticker);
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        // Получаем номер телефона.
        if (update.getMessage().hasContact()) {
            authorPhoneNumber = update.getMessage().getContact().getPhoneNumber();
            System.out.println(authorName + " отправил свой номер телефона.");
            //Пишем номер инфу в БД.
            try {
                dbManager.DbConnection();
                dbManager.DbCRUD();
                dbManager.connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            //отправляем ответ и удаляем кнопки.
            try {
                execute(sendMessage.setText("Спасибо, я запомню его \uD83D\uDE0A").setReplyMarkup(new ReplyKeyboardRemove().setSelective(true)));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        //Если в сообщении стикер
        if (update.getMessage().hasSticker()) {
            //Получили стикер.
            Sticker sticker = update.getMessage().getSticker();
            System.out.println(authorName + " прислал стикер. Его FileId - " + sticker.getFileId());
            try {
                //Отвечаем фразу + эмодзи полученного стикера.
                execute(sendMessage.setText("О, это же стикер! " + sticker.getEmoji()));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    public String getBotUsername() {
        return ConfigParser.getBotUserName();
    }

    public String getBotToken() {
        return ConfigParser.getBotToken();
    }
}