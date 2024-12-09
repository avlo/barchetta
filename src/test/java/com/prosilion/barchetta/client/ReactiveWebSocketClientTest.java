//package com.prosilion.barchetta.client;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import nostr.base.PublicKey;
//import nostr.event.tag.EventTag;
//import nostr.event.tag.GeohashTag;
//import nostr.event.tag.HashtagTag;
//import nostr.event.tag.PubKeyTag;
//import nostr.event.tag.SubjectTag;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestInstance;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.math.BigDecimal;
//import java.time.Duration;
//
//import static org.awaitility.Awaitility.await;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//@SpringBootTest
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class ReactiveWebSocketClientTest {
//  private static final String PRV_KEY_VALUE = "23c011c4c02de9aa98d48c3646c70bb0e7ae30bdae1dfed4d251cbceadaeeb7b";
//  private static final String RELAY_URI = "wss://localhost:5555";
//  private static final String SUBSCRIBER_ID = "NostrWebSocketClientTest-subscriber_001";
//
//  public static final String ID = "299ab85049a7923e9cd82329c0fa489ca6fd6d21feeeac33543b1237e14a9e07";
//  public static final String KIND = "30402";
//  public static final String CLASSIFIED_CONTENT = "classified content";
//  public static final String PUB_KEY = "cccd79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984";
//  public static final String CREATED_AT = "1726114798510";
//  public static final String E_TAG_HEX = "494001ac0c8af2a10f60f23538e5b35d3cdacb8e1cc956fe7a16dfa5cbfc4346";
//  public static final String G_TAG_VALUE = "classified geo-tag-1";
//  public static final String T_TAG_VALUE = "classified hash-tag-1111";
//  public static final String P_TAG_HEX = "2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984";
//  public static final String SUBJECT = "classified subject";
//  public static final String TITLE = "classified title";
//  public static final String SUMMARY = "classified summary";
//  public static final String LOCATION = "classified location";
//  public static final String SIGNATURE = "86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546";
//
//  public static final SubjectTag SUBJECT_TAG = new SubjectTag(SUBJECT);
//  public static final EventTag E_TAG = new EventTag(E_TAG_HEX);
//  public static final PubKeyTag P_TAG = new PubKeyTag(new PublicKey(P_TAG_HEX));
//  public static final GeohashTag G_TAG = new GeohashTag(G_TAG_VALUE);
//  public static final HashtagTag T_TAG = new HashtagTag(T_TAG_VALUE);
//
//  public static final String PRICE_NUMBER = "271.00";
//  public static final String CURRENCY = "BTC";
//  public static final String FREQUENCY = "1";
//  public static final BigDecimal NUMBER = new BigDecimal(PRICE_NUMBER);
//
//  @Autowired
//  ReactiveWebSocketClient reactiveWebSocketClient;
//
//  @BeforeEach
//  void setup() {
//    assertEquals(expectedEventResponseJson(ID), reactiveWebSocketClient.send(eventJson()).stream().findFirst().get());
//  }
//
//  @Test
//  void testSendRequestExpectEventResponse() throws JsonProcessingException {
//    assertTrue(
//        JsonComparator.equalsJson(
//            new ObjectMapper().readTree(
//                expectedRequestResponseJson()
//            ),
//            new ObjectMapper().readTree(
//                reactiveWebSocketClient
//                    .send(
//                        createReqJson(SUBSCRIBER_ID, ID))
//                    .stream().findFirst().get()
//            )));
//    await().atMost(Duration.ofSeconds(3));
//  }
//
//  private String expectedEventResponseJson(String eventId) {
//    return "[\"OK\",\"" + eventId + "\",true,\"success: request processed\"]";
//  }
//
//  private String createReqJson(String subscriberId, String id) {
//    return "[\"REQ\",\"" + subscriberId + "\",{\"ids\":[\"" + id + "\"]}]";
//  }
//
//  private String expectedRequestResponseJson() {
//    return
//        "   [\"EVENT\",\"" + SUBSCRIBER_ID + "\",\n" +
//            "          {\"id\": \"" + ID + "\",\n" +
//            "          \"kind\": " + KIND + ",\n" +
//            "          \"content\": \"" + CLASSIFIED_CONTENT + "\",\n" +
//            "          \"pubkey\": \"" + PUB_KEY + "\",\n" +
//            "          \"created_at\": " + CREATED_AT + ",\n" +
//            "          \"tags\": [\n" +
////            "            [ \"e\", \"" + E_TAG.getIdEvent() + "\", \"" + E_TAG.getMarker() + "\" ],\n" +
//            "            [ \"e\", \"" + E_TAG.getIdEvent() + "\" ],\n" +
//            "            [ \"g\", \"" + G_TAG.getLocation() + "\" ],\n" +
//            "            [ \"t\", \"" + T_TAG.getHashTag() + "\" ],\n" +
//            "            [ \"price\", \"" + NUMBER + "\", \"" + CURRENCY + "\", \"" + FREQUENCY + "\" ],\n" +
//            "            [ \"p\", \"" + P_TAG.getPublicKey() + "\" ],\n" +
//            "            [ \"subject\", \"" + SUBJECT + "\" ],\n" +
//            "            [ \"title\", \"" + TITLE + "\" ],\n" +
//            "            [ \"published_at\", \"" + CREATED_AT + "\" ],\n" +
//            "            [ \"summary\", \"" + SUMMARY + "\" ],\n" +
//            "            [ \"location\", \"" + LOCATION + "\" ]\n" +
//            "          ],\n" +
//            "          \"sig\": \"" + SIGNATURE + "\"\n" +
//            "        }]";
//  }
//
//  private String eventJson() {
//    return """
//        [ "EVENT",
//          {
//            "id": "299ab85049a7923e9cd82329c0fa489ca6fd6d21feeeac33543b1237e14a9e07",
//            "kind": 30402,
//            "content": "classified content",
//            "pubkey": "cccd79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984",
//            "created_at": 1726114798510,
//            "tags": [
//              [ "e", "494001ac0c8af2a10f60f23538e5b35d3cdacb8e1cc956fe7a16dfa5cbfc4346" ],
//              [ "g", "classified geo-tag-1" ],
//              [ "t", "classified hash-tag-1111" ],
//              [ "price", "271.00", "BTC", "1" ],
//              [ "p", "2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984" ],
//              [ "subject", "classified subject" ],
//              [ "title", "classified title" ],
//              [ "published_at", "1726114798510" ],
//              [ "summary", "classified summary" ],
//              [ "location", "classified location" ]
//            ],
//            "sig": "86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546"
//          }
//        ]
//        """;
//  }
//}
