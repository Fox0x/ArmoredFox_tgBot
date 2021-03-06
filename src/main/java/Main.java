import config.ConfigParser;
import org.apache.log4j.PropertyConfigurator;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class Main {
    public static void main(String[] args) {
        PropertyConfigurator.configure(System.getProperty("user.dir")+ "/src/main/resources/log4j/log4j.properties");
        
        ConfigParser.load(); //Загружаем данные из конфига.
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        ApiContextInitializer.init();
        try {
            telegramBotsApi.registerBot(new Bot());
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }
}
