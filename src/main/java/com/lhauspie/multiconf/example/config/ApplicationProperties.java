package com.lhauspie.multiconf.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "app.properties")
public class ApplicationProperties {

  @ConstructorBinding
  public ApplicationProperties(String firstProperty, String secondProperty) {
    System.out.println("first property: " + firstProperty);
    System.out.println("second property: " + secondProperty);
  }
}
