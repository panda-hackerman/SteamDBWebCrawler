package dev.michaud.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Hidden {

  @JsonProperty("userAgent")
  private String userAgent;

  @JsonProperty("testUrl")
  private String testUrl;

  @JsonProperty("cookies")
  private Map<String, String> cookies;

  public static Hidden generate() {

    File file = new File("src/main/resources/hidden.yaml");

    if (!file.isFile()) {
      throw new RuntimeException("Missing hidden.yaml");
    }

    try {
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      return mapper.readValue(file, Hidden.class);
    } catch (IOException ex) {
      throw new RuntimeException("Couldn't read hidden.yaml due to an I/O Exception! Terminating.");
    }

  }

  public String userAgent() {
    return userAgent;
  }

  public String testUrl() {
    return testUrl;
  }

  public Map<String, String> cookies() {
    return ImmutableMap.copyOf(cookies);
  }

}