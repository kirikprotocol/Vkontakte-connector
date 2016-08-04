package com.eyelinecom.whoisd.sads2.vk.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: zoldorn
 * Date: 02.08.16
 * Time: 9:17
 * To change this template use File | Settings | File Templates.
 */
public class VkPlace {

  @JsonProperty(value = "title")
  private String title;

  @JsonProperty(value = "country")
  private String country;

  @JsonProperty(value = "city")
  private String city;

}
