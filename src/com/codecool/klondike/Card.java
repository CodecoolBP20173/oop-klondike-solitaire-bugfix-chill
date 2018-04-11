package com.codecool.klondike;

import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.util.*;

public class Card extends ImageView {

    private Suit suit;
    private Rank rank;
    private boolean faceDown;

    private Image backFace;
    private Image frontFace;
    private Pile containingPile;
    private DropShadow dropShadow;

    static Image cardBackImage;
    private static final Map<String, Image> cardFaceImages = new HashMap<>();
    public static final int WIDTH = 150;
    public static final int HEIGHT = 215;

    public Card(Suit suit, Rank rank, boolean faceDown) {
        this.suit = suit;
        this.rank = rank;
        this.faceDown = faceDown;
        this.dropShadow = new DropShadow(2, Color.gray(0, 0.75));
        backFace = cardBackImage;
        frontFace = cardFaceImages.get(getShortName());
        setImage(faceDown ? backFace : frontFace);
        setEffect(dropShadow);
    }

    public int getSuit() {
        return suit.getValue();
    }

    public int getRank() {
        return rank.getValue();
    }

    public boolean isFaceDown() {
        return faceDown;
    }

    public String getShortName() {
        return "S" + suit.getValue() + "R" + rank.getValue();
    }

    public DropShadow getDropShadow() {
        return dropShadow;
    }

    public Pile getContainingPile() {
        return containingPile;
    }

    public void setContainingPile(Pile containingPile) {
        this.containingPile = containingPile;
    }

    public void moveToPile(Pile destPile) {
        this.getContainingPile().getCards().remove(this);
        destPile.addCard(this);
    }

    public void flip() {
        faceDown = !faceDown;
        setImage(faceDown ? backFace : frontFace);
    }

    @Override
    public String toString() {
        return "The " + "Rank" + rank.getValue() + " of " + "Suit" + suit.getValue();
    }

    public static boolean isOppositeColor(Card card1, Card card2) {
        //TODO
        return ((card1.suit.getValue() - 1) / 2 != (card2.suit.getValue() - 1) / 2);
    }

    public static boolean isSameSuit(Card card1, Card card2) {
        return card1.getSuit() == card2.getSuit();
    }

    public static List<Card> createNewDeck() {
        List<Card> result = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                result.add(new Card(suit, rank, true));
            }
        }
        return result;
    }

    public static void loadCardImages() {
        cardBackImage = new Image("card_images/card_back.png");
        for (Suit suit : Suit.values()) {
            String suitName = suit.name();
            for (Rank rank : Rank.values()) {
                String cardName = suitName + rank.getValue();
                System.out.println(cardName);
                String cardId = "S" + suit + "R" + rank;
                String imageFileName = "card_images/" + cardName + ".png";
                cardFaceImages.put(cardId, new Image(imageFileName));
            }
        }
    }

    public enum Suit {
        hearts(1),
        diamonds(2),
        spades(3),
        clubs(4);

        private final int value;

        Suit(int value) {
            this.value = value;
        }

        public int getSuitAsInt() {
            return value;
        }

        public String getSuitAsString() {
            return String.valueOf(value);
        }

        public static Suit convertIntToSuit(int iSuit) {
            for (Suit suit : Suit.values()) {
                if (suit.getSuitAsInt() == iSuit) {
                    return suit;
                }
            }
            return null;
        }

        public static Suit convertStringToSuit(String inputSuit) {
            for (Suit suit : Suit.values()) {
                if (suit.getSuitAsString().equals(inputSuit)) {
                    return suit;
                }
            }
            return null;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Rank {
        ace(1),
        two(2),
        three(3),
        four(4),
        five(5),
        six(6),
        seven(7),
        eight(8),
        nine(9),
        ten(10),
        jack(11),
        queen(12),
        king(13);

        private final int value;

        Rank(int value) {
            this.value = value;
        }

        public int getRankAsInt() {
            return value;
        }

        public String getRankAsString() {
            return String.valueOf(value);
        }

        public static Rank convertIntToRank(int iRank) {
            for (Rank rank : Rank.values()) {
                if (rank.getRankAsInt() == iRank) {
                    return rank;
                }
            }
            return null;
        }

        public static Rank convertStringToSuit(String inputSuit) {
            for (Rank rank : Rank.values()) {
                if (rank.getRankAsString().equals(inputSuit)) {
                    return rank;
                }
            }
            return null;
        }

        public int getValue() {
            return value;
        }
    }
}
