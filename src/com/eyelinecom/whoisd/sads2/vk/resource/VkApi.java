package com.eyelinecom.whoisd.sads2.vk.resource;

import com.eyelinecom.whoisd.sads2.vk.api.types.VkMessagesGet;
import com.eyelinecom.whoisd.sads2.vk.api.types.VkUsersGet;

/**
 * Created with IntelliJ IDEA.
 * User: zoldorn
 * Date: 18.07.16
 * Time: 6:05
 * To change this template use File | Settings | File Templates.
 */
public interface VkApi {

  void send(String message, int userId, String accessToken);

  void send(String message, int userId, String accessToken, String latitude, String longitude);

  String connectorUrl();

  String getCallbackConfirmationCode(String token, String groupId);

  int setCallbackServer(String token, String groupId, String url);

  void sendAttachment(String attachmentId, int userId, String token);

  String getCallbackServer(String token, String groupId);

  String uploadPhoto(String token, byte[] bytes);

  VkMessagesGet getMessages(String accessToken);

  VkUsersGet groupsGetManagers(int groupId, String accessToken);
}
