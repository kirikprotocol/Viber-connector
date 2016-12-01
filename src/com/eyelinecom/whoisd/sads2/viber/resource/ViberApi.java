package com.eyelinecom.whoisd.sads2.viber.resource;

import com.eyelinecom.whoisd.sads2.viber.api.types.ViberAccountInfoResponse;
import com.eyelinecom.whoisd.sads2.viber.api.types.ViberMessage;
import com.eyelinecom.whoisd.sads2.viber.api.types.ViberRequest;
import com.eyelinecom.whoisd.sads2.viber.api.types.ViberResponse;

/**
 * Created with IntelliJ IDEA.
 * User: zoldorn
 
 * Time: 6:05
 
 */
public interface ViberApi {

  String connectorUrl();

  ViberAccountInfoResponse getAccountInfo(String authToken);

  ViberResponse subscribeWebhook(String authToken, String webhookUrl);

  ViberResponse sendMessage(ViberMessage message);

  void sendMessage(String msg, String receiver, String authToken);


}
