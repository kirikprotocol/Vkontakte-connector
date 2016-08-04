package com.eyelinecom.whoisd.sads2.vk.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: zoldorn
 * Date: 02.08.16
 * Time: 9:16
 * To change this template use File | Settings | File Templates.
 */
public class VkGeo {
  private static final Pattern COORDINATES_PATTERN = Pattern.compile("([0-9]+\\.[0-9]+) ([0-9]+\\.[0-9]+)");
  @JsonProperty(value = "type")
  private String type;

  @JsonProperty(value = "coordinates")
  private String coordinates;

  @JsonProperty(value = "place")
  private VkPlace place;

  private double latitude = Double.NaN;
  private double longitude = Double.NaN;

  public boolean hasCoordinates() {
    parseCoordinates();
    return !Double.isNaN(latitude) && !Double.isNaN(longitude);
  }

  private void parseCoordinates() {
    if (coordinates == null) return;
    Matcher m = COORDINATES_PATTERN.matcher(coordinates);
    if (!m.matches()) return;
    latitude = Double.parseDouble(m.group(1));
    longitude = Double.parseDouble(m.group(2));
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }
}
