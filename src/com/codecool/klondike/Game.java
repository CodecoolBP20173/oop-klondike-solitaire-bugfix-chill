package com.codecool.klondike;

import javafx.beans.Observable;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.ButtonBar;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.application.Platform;

import java.util.*;

public class Game extends Pane {

    private List<Card> deck;

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;

    private static Card lastClicked;


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        }
        else if (lastClicked != null) {
            if (card == lastClicked && card == card.getContainingPile().getTopCard() && !foundationPiles.contains(card.getContainingPile())) {
                Pile foundation = null;
                for (Pile pile : foundationPiles) {
                    if (!pile.equals(card.getContainingPile()) && isMoveValid(card, pile)) {
                        foundation = pile;
                        break;
                    }
                }
                if (foundation != null) {
                    draggedCards.add(card);
                    handleValidMove(card, foundation);
                    lastClicked = null;
                }
                else {
                    lastClicked = card;
                }
            }
            else {
                lastClicked = card;
            }
        }
        else {
            lastClicked = card;
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
        lastClicked = null;
        Pile activePile = card.getContainingPile();
        if ((activePile.getPileType() == Pile.PileType.STOCK) ||
            (activePile.getPileType() == Pile.PileType.DISCARD && card != activePile.getTopCard()) ||
            (activePile.getPileType() == Pile.PileType.FOUNDATION && card != activePile.getTopCard())) {
            return;
        }

        draggedCards.clear();
        card.getContainingPile().getCards().indexOf(card);
        draggedCards.addAll(activePile.getCards().subList(activePile.getCards().indexOf(card), activePile.numOfCards()));

        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;
        for (Card card1 : draggedCards){

            card1.getDropShadow().setRadius(20);
            card1.getDropShadow().setOffsetX(10);
            card1.getDropShadow().setOffsetY(10);

            card1.toFront();
            card1.setTranslateX(offsetX);
            card1.setTranslateY(offsetY);
        }

    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;
        Card card = draggedCards.get(0);
        Pile fund = getValidIntersectingPile(card, foundationPiles);
        Pile tabl = getValidIntersectingPile(card, tableauPiles);
        //TODO
        if (fund != null && draggedCards. size() == 1) {
            handleValidMove(card, fund);
        }
        else if (tabl != null) {
            handleValidMove(card, tabl);
        }
        else {
            Iterator<Card> draggedIterator = draggedCards.iterator();

            while (draggedIterator.hasNext()) {
                Card actualCard = draggedIterator.next();
                MouseUtil.slideBack(actualCard);
            }
            draggedCards.clear();
            lastClicked = null;
        }
    };

    public boolean isGameWon() {
        //TODO
        for (Pile pile : foundationPiles) {
            if (pile.getTopCard().getRank() != Card.Rank.king.getValue()) {
                return false;
            }
        }
        return true;
    }

    private void flipTopCards() {
        Iterator<Pile> tableauIterator = tableauPiles.iterator();

        while (tableauIterator.hasNext()) {
            Pile tableau = tableauIterator.next();
            if (tableau.numOfCards() > 0) {
                if (tableau.getTopCard().isFaceDown()) {
                    tableau.getTopCard().flip();
                    addMouseEventHandlers(tableau.getTopCard());
                }
            }
        }
    }

    private void shuffleDeck() {
        Collections.shuffle(deck);
    }

    public Game() {
        deck = Card.createNewDeck();
        shuffleDeck();
        initPiles();
        dealCards();
        tableauPiles.forEach(pile -> pile.getCards().addListener(new ListChangeListener<Card>() {
            @Override
            public void onChanged(Change<? extends Card> c) {
                // TODO - check for .isEmpty() before topcard
                if (!pile.isEmpty()) {
                    if (pile.getTopCard().isFaceDown()) {
                        pile.getTopCard().flip();
                        addMouseEventHandlers(pile.getTopCard());
                    }
                }
            }
        }));
        foundationPiles.forEach(pile -> pile.getCards().addListener(new ListChangeListener<Card>() {
            @Override
            public void onChanged(Change<? extends Card> c) {
                for (Pile pile : foundationPiles) {
                    if (pile.getTopCard().getRank() != Card.Rank.king.getValue()) {
                        return;
                    }
                }
                // TODO - repair newGame issue
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Hey, Vsauce. Michael here...");
                    alert.setHeaderText("Congratulation! You won!");
                    alert.setContentText("Would you like to play a new game?");

                    ButtonType buttonTypeNewGame = new ButtonType("New Game");
                    ButtonType buttonTypeReset = new ButtonType("Reset");
                    ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                    alert.getButtonTypes().setAll(buttonTypeNewGame, buttonTypeReset, buttonTypeCancel);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == buttonTypeNewGame) {
                        // ... user chose "New Game"
                        // TODO - here's the problem...
                        newGame();
                    } else if (result.get() == buttonTypeReset) {
                        // ... user chose "Reset"
                    } else {
                        // ... user chose CANCEL or closed the dialog
                    }
                });
                /*
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Hey, Vsauce. Michael here...");
                    alert.setHeaderText("Congratulation!");
                    alert.setContentText("You won!");
                    alert.showAndWait().ifPresent(rs -> {
                    if (rs == ButtonType.OK) {
                        System.out.println("Pressed OK.");
                    }
                    });
                });
                */
            }
        }));
    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {
        //TODO
        if (stockPile.isEmpty()) {
            while (discardPile.numOfCards() > 0) {
                Card card = discardPile.getTopCard();
                card.moveToPile(stockPile);
                card.flip();
            }
        }
        lastClicked = null;
        System.out.println("Stock refilled from discard pile.");
    }

    public boolean isMoveValid(Card card, Pile destPile) {
        //TODO
        if (destPile.getPileType().equals(Pile.PileType.FOUNDATION)) {
            if (destPile.isEmpty()) {
                return card.getRank() == 1;
            }
            else if (destPile.getTopCard().getRank() + 1 == card.getRank()) {
                return destPile.getTopCard().getSuit() == card.getSuit();
            }

        } else if (destPile.getPileType().equals(Pile.PileType.TABLEAU)) {
            if (destPile.isEmpty()) {
                return (card.getRank() == Card.Rank.king.getValue());
            }
            else if (destPile.getTopCard().getRank() - 1 == card.getRank()) {
                return Card.isOppositeColor(destPile.getTopCard(), card);
            }
        }
        return false;
    }

    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }
        System.out.println(msg);
        MouseUtil.slideToDest(draggedCards, destPile);
        draggedCards.clear();
        lastClicked = null;
    }

    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();
        //TODO
        Iterator<Pile> tableauIterator = tableauPiles.iterator();

        int size = 1;
        while (tableauIterator.hasNext()) {
            Pile tableau = tableauIterator.next();
            for (int i = 0; i < size; i++) {
                Card card = deckIterator.next();
                deckIterator.remove();
                tableau.addCard(card);
                getChildren().add(card);
            }
            size++;
        }
        flipTopCards();

        deckIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);
        });

    }

    public Game newGame() {
        deck.clear();
        tableauPiles.clear();
        foundationPiles.clear();
        stockPile.clear();
        discardPile.clear();
        draggedCards.clear();

        return new Game();
    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

}
