package com.eyelinecom.whoisd.sads2.viber;


import com.eyelinecom.whoisd.sads2.viber.api.types.ViberCallback;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public class ParseCallback {

  public static void main(String[] args) throws Exception {

    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    String json = "{\n" +
        "    \"event\": \"message\",\n" +
        "    \"message\": {\n" +
        "        \"media\": \"https://dl-media.viber.com/5/media/2/short/any/sig/image/0x0/7e10/f67094bcade20b91aa324abd0bd561114f17f0d42fd03522d19e9d1332db7e10.jpg?Expires=1479725303&Signature=EKw7lPC8XgJ2ewNcOrKpiKqF~wwU9qGIzD9CMhe8W3EQmMXtjqOVfg28nBeGkCnbhgf52Gv-pFIUuB8an80eQVGMhbACdjV1fWO8LpGF0pxFSx95V1UmIJdBut~QU2JwNeD8CCA~6-8B2PTc6TC7N8feJ3h1c24k8C71PPuzyEdurOVhACMSwBRizycG~YCk015Ua-RDhewZ3stC6kIe7i4BNla0IBOjb0~iP1p04aLCYCq4il8ILqL39Rw5xEIL6OkI4rATxwluCljv1ly4BJHqYHQRkq6-tTWS0XfN3jYM4EUdiParvvxCviibehrcytVSWB3F-qMbp~Q3CIdeIA__&Key-Pair-Id=APKAJ62UNSBCMEIPV4HA\",\n" +
        "        \"type\": \"picture\"\n" +
        "        \"text\": \"Frog\"\n" +
        "    },\n" +
        "    \"message_token\": MESSAGE_TOKEN,\n" +
        "    \"sender\": {\n" +
        "        \"avatar\": \"https://share.viber.com/download_photo?dlid=0-03-05-9f50a40a37af1ae582e20b7f38c852b413ab433b49a1e465bfc65e600ea74660%26fltp=jpg%26imsz=0000\",\n" +
        "        \"id\": \"B97ZnbvEiCGiMVv6jFM0DA==\",\n" +
        "        \"name\": \"Boris Bondarev\"\n" +
        "    },\n" +
        "    \"timestamp\": 1479721703201\n" +
        "}";

    ViberCallback cb = mapper.readerFor(ViberCallback.class).readValue(mapper.readTree(json));

    System.out.println("Parsed");

  }

}