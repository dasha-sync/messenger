package com.talk_wire.messenger.service;

import org.springframework.stereotype.Service;

import java.io.Console;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ChatService {
    private final String characters = "QWERT YUIOP ASDFG HJKLZ XCVBN Mqwer tyuiop asdfg hjkl zxcvb nm123 4567 890";

    public String answerMessage(String data) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            int index = ThreadLocalRandom.current().nextInt(characters.length());
            result.append(characters.charAt(index));
        }
        System.out.println("############### " + result.toString());
        return result.toString();
    }
}
