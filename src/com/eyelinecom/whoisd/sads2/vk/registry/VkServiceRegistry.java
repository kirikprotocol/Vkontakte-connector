package com.eyelinecom.whoisd.sads2.vk.registry;

import com.eyelinecom.whoisd.sads2.common.InitUtils;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.exception.ConfigurationException;
import com.eyelinecom.whoisd.sads2.registry.Config;
import com.eyelinecom.whoisd.sads2.registry.ServiceConfig;
import com.eyelinecom.whoisd.sads2.registry.ServiceConfigListener;
import com.eyelinecom.whoisd.sads2.resource.ResourceFactory;
import com.eyelinecom.whoisd.sads2.vk.resource.VkApi;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: gev
 * Date: 01.08.16
 * Time: 13:56
 * To change this template use File | Settings | File Templates.
 */
public class VkServiceRegistry extends ServiceConfigListener {

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VkServiceRegistry.class);

  public static final String CONF_TOKEN = "vkontakte.token";

  private final Map<String, ServiceEntry> serviceMap = new ConcurrentHashMap<>();
  private final VkApi api;

  public VkServiceRegistry(VkApi api) {
    this.api = api;
  }

  @Override
  protected void process(Config config) throws ConfigurationException {
    final String serviceId = config.getId();
    if (config.isEmpty()) {
      unregister(serviceId);
    } else if (config instanceof ServiceConfig) {
      final ServiceConfig serviceConfig = (ServiceConfig) config;
      String token = getAccessToken(serviceConfig.getAttributes());
      String group = getVkGroup(serviceConfig.getAttributes());
      String confirmationCode = null; // TODO:...
      token = StringUtils.trimToNull(token);
      group = StringUtils.trimToNull(group);
      confirmationCode = StringUtils.trimToNull(confirmationCode);
      if (token == null || group == null) {
        unregister(serviceId);
      } else {
        register(serviceId, token, group, confirmationCode);
      }
    }
  }

  private void register(String serviceId, String token, String groupId, String confirmationCode) {
    String url = api.connectorUrl() + "/" + serviceId;
    log.debug("registering serviceId: " + serviceId + ", groupId: " + groupId + ", token: " + token.substring(0, 6) + "..." +
      ", confirmationCode: " + confirmationCode + " url: " + url);
    if (confirmationCode != null) {
      serviceMap.put(serviceId, new ServiceEntry(serviceId, groupId, token, confirmationCode));
    } else {
      String currentUrl = api.getCallbackServer(token, groupId);
      if (!currentUrl.equals(url)) {
        confirmationCode = api.getCallbackConfirmationCode(token, groupId);
        log.debug("got confirmationCode: " + confirmationCode);
        serviceMap.put(serviceId, new ServiceEntry(serviceId, groupId, token, confirmationCode));
        int code;
        do {
          code = api.setCallbackServer(token, groupId, url);
          try {
            Thread.sleep(1000); // TODO: get rid of this shit (or make it look better)
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        } while (code == 2);
      } else {
        serviceMap.put(serviceId, new ServiceEntry(serviceId, groupId, token, confirmationCode));
      }
    }
  }

  private void unregister(String serviceId) {
    // TODO: ?
  }

  public String getConfirmationCode(String serviceId) {
    ServiceEntry entry = serviceMap.get(serviceId);
    if (entry == null) return null;
    return entry.confirmationCode;
  }

  public String getGroupId(String serviceId) {
    ServiceEntry entry = serviceMap.get(serviceId);
    if (entry == null) return null;
    return entry.groupId;
  }

  public static String getAccessToken(Properties properties) {
    VkBotSettings botSettings = getVkBotSettings(properties);
    if(botSettings == null) return null;
    return botSettings.accessToken;
  }

  public static String getVkGroup(Properties properties) {
    VkBotSettings botSettings = getVkBotSettings(properties);
    if(botSettings == null) return null;
    return "" + botSettings.groupId;
  }

  private static VkBotSettings getVkBotSettings(Properties properties) {
    return VkBotSettings.get(properties.getProperty(VkServiceRegistry.CONF_TOKEN));
  }

  public static class Factory implements ResourceFactory {

    @Override
    public Object build(String id, Properties properties, HierarchicalConfiguration config) throws Exception {
      VkApi api = SADSInitUtils.getResource("vkontakte-api", properties);
      return new VkServiceRegistry(api);
    }

    @Override
    public boolean isHeavyResource() {
      return false;
    }
  }

  private static class ServiceEntry {

    private final String serviceId;
    private final String groupId;
    private final String token;
    private final String confirmationCode;

    public ServiceEntry(String serviceId, String groupId, String token, String confirmationCode) {
      this.serviceId = serviceId;
      this.groupId = groupId;
      this.token = token;
      this.confirmationCode = confirmationCode;
    }
  }
}
