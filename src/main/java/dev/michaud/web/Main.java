package dev.michaud.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.michaud.web.RandomGamePicker.App;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

/**
 * This code sucks ass, I know, but I don't plan on ever maintaining it once I get the files I need,
 * sorry. I don't have time to write less code.
 */
public class Main {

  @JsonIgnoreProperties(ignoreUnknown = true)
  static class ReleaseDate {

    @JsonProperty("coming_soon")
    public boolean comingSoon;
    @JsonProperty("date")
    public String date;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  static class AppData {

    @JsonProperty("name")
    public String name;
    @JsonProperty("steam_appid")
    public int appid;
    @JsonProperty("is_free")
    public boolean isFree;
    @JsonProperty("release_date")
    private ReleaseDate releaseDate;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  static class AppDetails {

    @JsonProperty("success")
    public boolean success;

    @JsonProperty("data")
    private AppData data;
  }

  public static final String CSV_SEPARATOR = ",";
  public static Map<String, String> cookies = new HashMap<>();
  public static Logger logger = Logger.getGlobal();
  public static Hidden hidden = Hidden.generate();

  public static void main(String[] args) throws IOException {

    int appsToGet = 50;
    int timeout = 200;

    List<AppDetails> apps = new ArrayList<>(appsToGet);

    while (timeout != 0 && apps.size() < appsToGet) {
      timeout--;

      App app = RandomGamePicker.GetRandom();
      System.out.println("Chose random app with ID: " + app.appId);
      AppDetails details = GetDetails(app);

      if (details == null) {
        System.out.println("Failed to retrieve details... Skipping...");
        continue;
      }

      if (!details.success) {
        System.out.println("Failed... Skipping...");
        continue;
      }

      if (details.data.releaseDate.comingSoon) {
        System.out.println("Game is coming soon, skipping...");
        continue;
      }

      apps.add(details);
    }

    WriteAppDetailsToCSV(apps);
  }

  private static void WriteAppDetailsToCSV(Iterable<AppDetails> apps) {

    try {

      BufferedWriter bw = new BufferedWriter(
          new OutputStreamWriter(new FileOutputStream("output.csv"), StandardCharsets.UTF_8));
      String header = "\"Steam AppId\",\"Name\",\"Free?\",\"Release Date\"";

      bw.write(header);
      bw.newLine();

      for (AppDetails details : apps) {
        String builder = "\"" + details.data.appid + "\"" + CSV_SEPARATOR
            + "\"" + details.data.name + "\"" + CSV_SEPARATOR
            + (details.data.isFree ? "\"Yes\"" : "\"No\"") + CSV_SEPARATOR
            + "\"" + details.data.releaseDate.date + "\"";

        bw.write(builder);
        bw.newLine();
      }

      bw.flush();
      bw.close();
    } catch (Exception e) {
      //ignored
    }

  }

  private static AppDetails GetDetails(App app) throws IOException {

    ObjectMapper mapper = new ObjectMapper();
    String infoUrl = "https://store.steampowered.com/api/appdetails?appids=" + app.appId;
    Response appInformation = requestUrl(infoUrl);

    if (appInformation == null) {
      return null;
    }

    TypeReference<HashMap<String, AppDetails>> typeRef = new TypeReference<>() {
    };
    HashMap<String, AppDetails> obj = mapper.readValue(appInformation.body(), typeRef);

    return obj.get(String.valueOf(app.appId));
  }

  private static @Nullable Response requestUrl(String url) throws IOException {

    Response response = Jsoup.connect(url)
        .ignoreContentType(true)
        .userAgent(hidden.userAgent())
        .referrer("http://www.google.com")
        .header("Accept-Language", "en")
        .timeout(12000)
        .followRedirects(true)
        .cookies(cookies)
        .execute();
    cookies.putAll(response.cookies());

    printStatus(response);

    if (response.statusCode() == 200) {
      return response;
    } else {
      return null; //Something went wrong
    }
  }

  private static void printStatus(@NotNull Response response) {

    URL url = response.url();
    int status = response.statusCode();

    if (status == 200) {
      logger.info(String.format("200 (OK); at url: \"%s\"", url));
    } else if (status >= 400 && status < 500) {
      logger.warning(String.format("%s (CLIENT ERROR); at url: \"%s\"", status, url));
    } else if (status >= 500) {
      logger.warning(String.format("%s (SERVER ERROR); at url: \"%s\"", status, url));
    } else {
      logger.warning(String.format("Unexpected response %s; at url: \"%s\"", status, url));
    }

  }

}