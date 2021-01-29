package dylan;
/*
Dylan Grinton
Jan 26 2021
blackjack
 */

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class FXMLController implements Initializable {

    // Score Labels
    @FXML private Label you;
    @FXML private Label bot;
    @FXML private Label outcome;

    // Your Cards
    @FXML private ImageView youC1;
    @FXML private ImageView youC2;
    @FXML private ImageView youC3;
    @FXML private ImageView youC4;
    @FXML private ImageView youC5;
    @FXML private ImageView youC6;
    @FXML private ImageView youC7;
    @FXML private ImageView youC8;
    @FXML private ImageView youC9;
    @FXML private ImageView youC10;
    @FXML private ImageView youC11;

    // Bot's Cards
    @FXML private ImageView botC1;
    @FXML private ImageView botC2;
    @FXML private ImageView botC3;
    @FXML private ImageView botC4;
    @FXML private ImageView botC5;
    @FXML private ImageView botC6;
    @FXML private ImageView botC7;
    @FXML private ImageView botC8;
    @FXML private ImageView botC9;
    @FXML private ImageView botC10;
    @FXML private ImageView botC11;

    // Buttons
    @FXML private Button start;
    @FXML private Button hit;
    @FXML private Button stand;
    @FXML private Button colour;
    @FXML private Button restart;
    @FXML private Button help;

    // Bet Objects
    @FXML private Button plus10;
    @FXML private Button plus100;
    @FXML private Button plus1000;
    @FXML private Button plusAll;
    @FXML private Button min10;
    @FXML private Button min100;
    @FXML private Button min1000;
    @FXML private Button minAll;
    @FXML private TextField betField;
    @FXML private Label balance;

    @FXML private Label helpText;

    // Array of the card's paths
    private final String[] cards = new String[52];

    // Hands of each player
    int[] youCards = new int[11];
    int[] botCards = new int[11];

    int youAces = 0;
    int botAces = 0;

    int chips = 1000;
    int bet = 0;

    String back = "/back.png";

    // ImageViews for each player's hands
    ImageView[] youImages = new ImageView[11];
    ImageView[] botImages = new ImageView[11];

    @FXML
    void start(ActionEvent event) {
        bet = Integer.parseInt(betField.getText());
        helpText.setVisible(false);

        // Reset Hands
        resetCardImages(youImages);
        resetCardImages(botImages);
        botC2.setImage(scale(back));

        // Resets lengths
        youCards = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        botCards = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

        // Sets images of cards
        youC1.setImage(scale(newCardImage(youCards)));
        botC1.setImage(scale(newCardImage(botCards)));
        youC2.setImage(scale(newCardImage(youCards)));

        // 2nd Dealer card is hidden
        botCards[1] = newCard();

        // Updates score label
        int youValue = getValue(youCards[0]) + getValue(youCards[1]);
        int botValue = getValue(botCards[0]) + getValue(botCards[1]);
        you.setText(Integer.toString(youValue));
        bot.setText(Integer.toString(getValue(botCards[0])));

        menuVisibility(false);

        // Changes ace counter
        youAces = startingAces(youCards, youValue, you);
        botAces = startingAces(botCards, botValue, bot);

        // Checks for blackjacks
        if (botValue == 21) {
            // Reveals hole card if dealer has a blackjack
            botC2.setImage(scale(cards[botCards[1]]));

            // Checks for player's blackjack
            if (youValue == 21) {
                endRound("Tie! (Push)", 0);
            } else {
                endRound("You Lose! (Dealer's Blackjack)", -bet);
            }

            // If only player has a blackjack
        } else if (youValue == 21) {
            endRound("You Win! (Blackjack)", (int) (bet * 1.5));
        }
    }

    @FXML
    void hit(ActionEvent event) {
        // Gets a new card
        youAces += newHit(youImages, youCards, you);
        int value = Integer.parseInt(you.getText());

        // Decrease value by 10 if you bust with an ace
        if (value > 21 && youAces > 0) {
            you.setText(Integer.toString(value - 10));
            --youAces;

            // Lose if you go over 21 && no aces
        } else if (value > 21) {
            endRound("You Lose! (You Bust)", -bet);
        }

        // Removes hit button if all ImageViews are in use
        if (youC11.isVisible()) {
            hit.setVisible(false);
        }
    }

    @FXML
    void stand(ActionEvent event) {
        // Puts hand values into variables for clarity
        int botValue = getValue(botCards[0]) + getValue(botCards[1]);
        int youValue = Integer.parseInt(you.getText());
        bot.setText(Integer.toString(botValue));

        // Keeps hitting until it gets a higher score, or is over 16
        while (botValue < youValue && botValue < 17) {
            botAces += newHit(botImages, botCards, bot);
            botValue = Integer.parseInt(bot.getText());

            // Decrease value by 10 if dealer busts with an ace
            if (botValue > 21 && botAces > 0)  {
                bot.setText(Integer.toString(botValue - 10));
                botValue -= 10;
                --botAces;
            }
        }
        // Updates Score
        bot.setText(Integer.toString(botValue));

        // Reveals hole card
        botC2.setImage(scale(cards[botCards[1]]));

        // Bot loses to bust
        if (botValue > 21) {
            endRound("You Win! (Dealer Bust)", bet);

            // Tie
        } else if (botValue == youValue) {
            endRound("Tie! (Push)", 0);

            // Bot Wins
        } else if (botValue > youValue) {
            endRound("You Lose!", -bet);

            // Bot loses to worse hand
        } else {
            endRound("You Win!", bet);
        }
    }

    @FXML
    void changeBet(ActionEvent event) {
        Button source = (Button) event.getSource();
        String buttonText = source.getText();

        if (buttonText.charAt(0) == '+') {
            // Prevents increasing bet above chips amount
            int inc = Integer.parseInt(buttonText.substring(1));
            if (Integer.parseInt(betField.getText()) + inc <= chips) {
                // Increase betField depending by amount depending on button clicked
                betField.setText(Integer.toString(Integer.parseInt(betField.getText()) + Integer.parseInt(buttonText.substring(1))));
            } else {
                betField.setText(Integer.toString(chips));
            }

        } else if (buttonText.charAt(0) == '-') {
            // Prevents subtracting bet to below 10
            int dec = Integer.parseInt(buttonText.substring(1));
            if (Integer.parseInt(betField.getText()) - dec >= 10) {
                // Decreases betField depending by amount depending on button clicked
                betField.setText(Integer.toString(Integer.parseInt(betField.getText()) - Integer.parseInt(buttonText.substring(1))));
            } else {
                betField.setText("10");
            }

            // Max/Min Bets
        } else if (buttonText.equals("Max")) {
            betField.setText(Integer.toString(chips));

        } else if (buttonText.equals("Min")) {
            betField.setText("10");
        }
    }

    @FXML
    void keyTyped(KeyEvent event) {
        int pos = betField.getCaretPosition();

        // Removes non-number character when typed
        String s = betField.getText();
        for (int i = 0; i < betField.getText().length(); i++) {
            try {
                // Tries to parse each character
                Integer.parseInt(String.valueOf(s.charAt(i)));
            } catch (NumberFormatException e) {
                s = s.replace(s.charAt(i), '\0');
            }
        }
        betField.setText(s);
        // Change bet if below 10 Chips
        if (!betField.getText().equals("")) {
            // If TextField has a number below 10
            if (Integer.parseInt(betField.getText()) < 10) {
                betField.setText(Integer.toString(10));

                // If TextField has a number above your balance
            } else if (Integer.parseInt(betField.getText()) > chips) {
                betField.setText(Integer.toString(chips));
            }
        } else {
            // Changes to 10 if you TextField is empty
            betField.setText(Integer.toString(10));
        }

        // Re-positions to where you were typing in the TextField
        betField.positionCaret(pos);
    }

    @FXML
    void colour(ActionEvent event) {
        switch (back) {
            case "/back.png":
                back = "/back2.png";
                break;
            case "/back2.png":
                back = "/back.png";
                break;
        }
        youC1.setImage(scale(back));
        youC2.setImage(scale(back));
        botC1.setImage(scale(back));
        botC2.setImage(scale(back));

        you.setText("");
        bot.setText("");
        outcome.setVisible(false);

        resetCardImages(youImages);
        resetCardImages(botImages);
    }

    @FXML
    void restart(ActionEvent event) {
        chips = 1000;
        balance.setText(chips + " Chips");
        menuVisibility(true);
        outcome.setText("");
        restart.setVisible(false);
    }

    @FXML
    void help(ActionEvent event) {
        // Changes visibility of help label
        helpText.setVisible(!helpText.isVisible());
    }

    @FXML
    void helpText(MouseEvent event) {
        // Changes visibility of help label
        helpText.setVisible(!helpText.isVisible());
    }

    private void endRound(String endText, int a) {
        // Runs when round ends
        outcome.setText(endText);

        chips += a;
        bet = 0;
        balance.setText(chips + " Chips");

        menuVisibility(true);
        betField.setText("10");

        // Reset position of card images only if they were moved
        if (youC1.getLayoutX() == 25) {
            moveCards(youImages, 155);
            youC1.setLayoutX(110);
            youC2.setLayoutX(205);
        }
        if (botC1.getLayoutX() == 25) {
            moveCards(botImages, 155);
            botC1.setLayoutX(110);
            botC2.setLayoutX(205);
        }

        // If you lose all chips
        if (chips < 10) {
            menuVisibility(false);
            hit.setVisible(false);
            stand.setVisible(false);

            youC1.setImage(scale(back));
            youC2.setImage(scale(back));
            botC1.setImage(scale(back));
            botC2.setImage(scale(back));
            you.setText("");
            bot.setText("");

            resetCardImages(youImages);
            resetCardImages(botImages);

            outcome.setVisible(true);
            outcome.setText("You don't have enough chips to bet!");
            restart.setVisible(true);
        }
    }

    private int startingAces(int[] hand, int value, Label score) {
        if (hand[0] < 4 && hand[1] < 4) {
            // 2 Aces
            score.setText(Integer.toString(value - 10));
            return 1;

            // 1 ace
        } else if (hand[0] < 4 || hand[1] < 4) {
            return 1;
        }
        // No aces
        return 0;
    }

    private int newHit(ImageView[] cardImages, int[] hand, Label score) {
        // Gets next available imageView
        ImageView card = nextAvailableImage(cardImages);

        // Puts card in ImageView
        card.setImage(scale(newCardImage(hand)));
        card.setVisible(true);

        // Moves ImageView for more space if 7 or more cards are hit
        if (youImages[6] == card) {
            moveCards(youImages, 0);
        } else if (botImages[6] == card) {
            moveCards(botImages, 0);
        }

        // Gets the index of the new card
        int index = hand[getIndex(card, cardImages)];

        // Updates score
        score.setText(Integer.toString(Integer.parseInt(score.getText()) + getValue(index)));

        // Returns amount of Aces
        if (index < 4) {
            return 1;
        } else {
            return 0;
        }
    }

    private void moveCards(ImageView[] cards, int inc) {
        // Moves cards to make room for more once you hit more than 6 cards
        int i = 1;
        for (ImageView card : cards) {
            if (card != cards[6]) {
                // Move cards
                card.setLayoutX(25 * i + inc);
                ++i;
            } else {
                // Breaks once you moved all necessary cards
                break;
            }
        }
    }

    private int getIndex(ImageView card, ImageView[] cardImages) {
        // Returns index of 'card' in 'cardImages'
        int i = 0;

        for (ImageView arrayCard : cardImages) {
            if (card == arrayCard) {
                return i;
            } else {
                // Increases index if not equal
                i++;
            }
        }
        // -1 if card is not found
        return -1;
    }

    private ImageView nextAvailableImage(ImageView[] cardImages) {
        // Returns the next visible ImageView
        for (ImageView IV : cardImages) {
            if (!IV.isVisible()) {
                return IV;
            }
        }
        return cardImages[5];
    }

    private void menuVisibility(boolean b1) {
        // Changes visibility of objects on menu/game
        outcome.setVisible(b1);
        help.setVisible(b1);
        colour.setVisible(b1);
        start.setVisible(b1);
        plus10.setVisible(b1);
        plus100.setVisible(b1);
        plus1000.setVisible(b1);
        plusAll.setVisible(b1);
        min10.setVisible(b1);
        min100.setVisible(b1);
        min1000.setVisible(b1);
        minAll.setVisible(b1);
        betField.setVisible(b1);
        balance.setVisible(b1);

        // Inverse for game
        hit.setVisible(!b1);
        stand.setVisible(!b1);
    }

    private void resetCardImages(ImageView[] cardImages) {
        // Reset Hand Visibility
        for (ImageView card : cardImages) {
            // Turns all invisible except for 2 starting cards
            card.setVisible(card == cardImages[0] || card == cardImages[1]);
        }
    }

    private int getValue(int card) {
        // Divide by 4 gets the card's value (but doesn't return over 10 for court cards)
        int v = (card / 4) + 1;
        if (v != 1) {
            return Math.min(v, 10);
        } else {
            return 11;
        }
    }

    private String newCardImage(int[] cardList) {
        // Returns string path of a random card and sets it to appropriate index
        int index = newCard();

        // Find next available index
        for (int i = 0; i < cardList.length; i++) {
            // Replaces value once a -1 (empty) is found
            if (cardList[i] == -1) {
                cardList[i] = index;
                break;
            }
        }
        // Returns image path
        return cards[index];
    }

    private int newCard() {
        // Gets a new card and returns it's index in cards[]
        int r = random(0, 52);

        // Checks if card is in use
        if (matching(youCards, r) || matching(botCards, r)) {
            // Runs again if card is in use
            return newCard();
        } else {
            // Returns index of card
            return r;
        }
    }

    private boolean matching(int[] cardList, int r) {
        // Checks if card picked is in use
        for (int c : cardList) {
            if (r == c) {
                // Returns true if a matching card is found
                return true;
            }
        }
        return false;
    }

    private int random(int min, int max) {
        return ThreadLocalRandom.current().nextInt(0, 52);
    }

    private Image scale(String path) {
        // Scales the image to remove blur on big ImageViews
        Image image = new Image(getClass().getResource(path).toString());

        // Horizontal / Vertical scale (multiplied by 3)
        int scaleH = (int) image.getWidth() * 3;
        int scaleV = (int) image.getHeight() * 3;

        // Creates new image using this scale and sets it to output imageview
        return new Image(getClass().getResource(path).toString(), scaleH, scaleV, true, false);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        int value = 1;
        String suit = "c";
        int i = 0;
        // Adds all cards to array
        do {
            // Combines value/suit variables to get file name
            cards[i] = "/" + value + suit + ".png";
            i++;

            // Adds next suit
            switch (suit) {
                case "c":
                    suit = "d";
                    break;
                case "d":
                    suit = "h";
                    break;
                case "h":
                    suit = "s";
                    break;
                case "s":
                    // Goes back to clubs and increases value
                    suit = "c";
                    ++value;
                    break;
            }
            // Stops once it reaches index 51 (all cards added)
        } while (i < 52);

        youImages = new ImageView[]{youC1, youC2, youC3, youC4, youC5, youC6, youC7, youC8, youC9, youC10, youC11};
        botImages = new ImageView[]{botC1, botC2, botC3, botC4, botC5, botC6, botC7, botC8, botC9, botC10, botC11};
    }
}
