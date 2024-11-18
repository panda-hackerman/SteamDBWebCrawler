package dev.michaud.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import org.jetbrains.annotations.NotNull;

public class RandomGamePicker {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class App {
    @JsonProperty("appid")
    public int appId;

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      App app = (App) o;
      return appId == app.appId;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(appId);
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Response {
    @JsonProperty("apps")
    public List<App> apps;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class SteamAppsFile {
    @JsonProperty("response")
    public Response response;

    public List<App> GetApps() {
      return response.apps;
    }
  }

  public static String[] filePaths = {
      "src/main/resources/steam_apps_1.json",
      "src/main/resources/steam_apps_2.json",
      "src/main/resources/steam_apps_3.json"
  };

  public static List<App> allApps = LoadApps();

  public static Random random = new Random();

  public static App GetRandom() {
    int index = random.nextInt(allApps.size());
    return allApps.get(index);
  }

  private static @NotNull List<App> LoadApps() {

    ObjectMapper mapper = new ObjectMapper();
    List<App> apps = new ArrayList<>(110000);

    for (String path : filePaths) {
      try {
        File file = new File(path);
        SteamAppsFile parsed = mapper.readValue(file, SteamAppsFile.class);
        apps.addAll(parsed.GetApps());
      } catch (IOException e) {
        System.out.println("Couldn't parse file :(" + e);
      }
    }

    return apps;
  }

}