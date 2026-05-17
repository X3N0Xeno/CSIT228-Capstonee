package com.capstone.javabox.csit228.games.ultimatettt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CardDeck {

    private static final Card.CardType[] COMMON_CARDS = {
            Card.CardType.SKIP_ENEMY_TURN,
            //Card.CardType.DELETE_1_ENEMY,
            Card.CardType.DELETE_2_ENEMY,
            Card.CardType.SUMMON_ALLY,
            Card.CardType.BLOCK_CELL,
            Card.CardType.STEAL,
            Card.CardType.REDRAW,
            //Card.CardType.ANCHOR,
            Card.CardType.DOUBLE_TURN
    };

    private static final Card.CardType[] RARE_CARDS = {
            Card.CardType.NUKE,
            Card.CardType.SWAP_SYMBOLS,
            Card.CardType.CLAIM,
            Card.CardType.MIRROR
    };

    private static final Random random = new Random();

    public static Card drawCard() {
        // 10% chance of rare
        if (random.nextInt(100) < 10) {
            return new Card(RARE_CARDS[random.nextInt(RARE_CARDS.length)]);
        } else {
            return new Card(COMMON_CARDS[random.nextInt(COMMON_CARDS.length)]);
        }
    }

    public static List<Card> drawHand(int count) {
        List<Card> hand = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            hand.add(drawCard());
        }
        return hand;
    }
}