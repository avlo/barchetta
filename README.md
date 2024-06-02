```java
`7MM"""Yp,      db      `7MM"""Mq.   .g8"""bgd `7MMF'  `7MMF'`7MM"""YMM MMP""MM""YMM MMP""MM""YMM   db      
  MM    Yb     ;MM:       MM   `MM..dP'     `M   MM      MM    MM    `7 P'   MM   `7 P'   MM   `7  ;MM:     
  MM    dP    ,V^MM.      MM   ,M9 dM'       `   MM      MM    MM   d        MM           MM      ,V^MM.    
  MM"""bg.   ,M  `MM      MMmmdM9  MM            MMmmmmmmMM    MMmmMM        MM           MM     ,M  `MM    
  MM    `Y   AbmmmqMA     MM  YM.  MM.           MM      MM    MM   Y  ,     MM           MM     AbmmmqMA   
  MM    ,9  A'     VML    MM   `Mb.`Mb.     ,'   MM      MM    MM     ,M     MM           MM    A'     VML  
.JMMmmmd9 .AMA.   .AMMA..JMML. .JMM. `"bmmmd'  .JMML.  .JMML..JMMmmmmMMM   .JMML.       .JMML..AMA.   .AMMA.
```
# Barchetta Smart-Contract Negotation Framework
  - Build atop Bitcoin/Lightning-Network [RGB protocol](https://rgb.tech/) and [Single-Use Seal UTXO's](https://docs.rgb.info/distributed-computing-concepts/single-use-seals) for contract-binding
  - Contract attributes provided per [Nostr](https://nostr.com/) NIPS:
    - [NIP-99](https://nostr-nips.com/nip-99) Classified Listings
    - [NIP-15](https://nostr-nips.com/nip-15) Nostr Marketplace  
    - [NIP-75](https://nostr-nips.com/nip-75) Zap Goals
    

#### Core technologies

  - Spring Boot [3.2.4](https://spring.io/projects/spring-boot) 
    - Spring Web MVC
    - Spring JPA / Hibernate
  - [Presto](https://github.com/avlo/presto) - User Authentication/Authorization and Application Security framework     
    - Spring Security
      - Pluggable Authentication & Authorization framework, (currently) supports:
        -   Standalone
        -   LDAP
        -   OAuth2/JWT
            -   Azure
            -   (additional pending)
  - [Nostr](https://nostr.com/) - "A simple, open protocol that enables global, decentralized, and censorship-resistant social media."
    - [SuperConductor](https://github.com/avlo/nostr-relay/) - Java Nostr-Relay Framework & Web Application

## Requirements

    $ java -version

>     openjdk version "20.0.1" 2023-04-18
>     OpenJDK Runtime Environment (build 20.0.1+9-29)
>     OpenJDK 64-Bit Server VM (build 20.0.1+9-29, mixed mode, sharing)

    $ mvn -version
>     Apache Maven 3.9.2 (c9616018c7a021c1c39be70fb2843d6f5f9b8a1c)
>     Maven home: ~/.sdkman/candidates/maven/current
>     Java version: 20.0.1, vendor: Oracle Corporation, runtime: ~/.sdkman/candidates/java/20.0.1-open
>     Default locale: en_US, platform encoding: UTF-8
>     OS name: "linux", version: "5.15.0-72-generic", arch: "amd64", family: "unix"

## Build and run project

    $ mvn spring-boot:run

## Using application framework
Register new user:

    localhost:8080/register

Login & Authenticate user:

    localhost:8080/login

Show existing users (once Authenticated):

    localhost:8080/users

Show available contracts / Engage contract:

    localhost:8080/contract/display

## Viewing DB contents

DB console:

    localhost:8080/h2-console/

*user: sa*  
*password: // blank*

Show all DB tables & entries:

    SELECT * FROM APPUSER;
    SELECT * FROM APPUSER_AUTHUSER;
    SELECT * FROM CONTRACT;
    SELECT * FROM AUTHORITIES;
    SELECT * FROM USERS;

(helpful / relationship-grokable CONTRACT table query)

    SELECT c.id, b.auth_user_name as creator, d.auth_user_name as counter_party, CASE WHEN c.creator_role = 0 THEN 'payER' WHEN c.creator_role = 1 THEN 'payEE' END as creator_role, CASE WHEN c.payer_state = 0 THEN 'approve' WHEN c.payer_state = 1 THEN 'veto' END as payer_state, CASE WHEN c.payee_state = 0 THEN 'approve' WHEN c.payee_state = 1 THEN 'veto' END as payee_state, c.text FROM CONTRACT c LEFT JOIN APPUSER_AUTHUSER b ON (c.app_user_id=b.app_user_id) LEFT JOIN APPUSER_AUTHUSER d ON (c.counter_party_id=d.app_user_id);
