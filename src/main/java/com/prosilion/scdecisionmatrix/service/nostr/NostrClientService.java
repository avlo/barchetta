package com.prosilion.scdecisionmatrix.service.nostr;

public interface NostrClientService<T> {
  String HTTPS = "https";
  String HTTP = "http";
  String WS = "ws";
  String HOST = "localhost";
  String PORT = "8081";

  Void getClassifiedListingEvent(String event);

  String nostrRequestString = "[\"EVENT\",{\"id\":\"d6173464e0688bb3f585f683e57fe1b95e1b47301172ccbe29b30a14ce358c70\",\"kind\":\"1\",\"content\":\"111111111111111111111111111111\",\"pubkey\":\"2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984\",\"created_at\":1711354281731,\"tags\":[[\"e\",\"494001ac0c8af2a10f60f23538e5b35d3cdacb8e1cc956fe7a16dfa5cbfc4346\"],[\"p\",\"2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984\"]],\"sig\":\"86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546\"}]";
}
