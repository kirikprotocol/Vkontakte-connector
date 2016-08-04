package com.eyelinecom.whoisd.sads2.vk.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: gev
 * Date: 01.08.16
 * Time: 19:20
 * To change this template use File | Settings | File Templates.
 */
public class VkUploadResponse {

  @JsonProperty(value = "server")
  private Integer server;

  @JsonProperty(value = "photo")
  private String photo;

  @JsonProperty(value = "hash")
  private String hash;

  public String getHash() {
    return hash;
  }

  public String getPhoto() {
    return photo;
  }

  public Integer getServer() {
    return server;
  }

  @Override
  public String toString() {
    return "VkUploadResponse{" +
      "hash='" + hash + '\'' +
      ", server='" + server + '\'' +
      ", photo='" + photo + '\'' +
      '}';
  }
}
