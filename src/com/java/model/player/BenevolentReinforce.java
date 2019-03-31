package com.java.model.player;

import com.java.model.cards.Card;
import com.java.model.gamedata.GameData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class BenevolentReinforce implements Reinforce {

    private ArrayList<ReinforcementPhaseState> reinforcementPhaseState;
    private ArrayList<Card> cardList;
    private Integer playerID;
    private String playerName;
    private Scanner input;
    private GameData gameData;
    private static final int MINIMUM_REINFORCEMENT_ARMY_NUMBER = 3;
    private static final int REINFORCEMENT_DIVISION_FACTOR = 3;

    public BenevolentReinforce(Integer playerID, String playerName) {
        this.playerID = playerID;
        this.playerName = playerName;
    }

    @Override
    public void startReinforcement() {

        ArrayList<Card> playerExchangeCards;
        playerExchangeCards = getValidCards();
        Integer totalReinforcementArmyCount = calculateTotalReinforcement(playerExchangeCards);
        ReinforcementPhaseState reinforcementPhase = new ReinforcementPhaseState();
        reinforcementPhase.setNumberOfArmiesReceived(totalReinforcementArmyCount);
        reinforcementPhaseState.add(reinforcementPhase);
        placeArmy(totalReinforcementArmyCount);
    }

    private ArrayList<Card> getValidCards() {
        ArrayList<Card> playerCardList = getPlayerCardList();
        ArrayList<Card> playerExchangeCards = new ArrayList<>();
        ArrayList<Card> cumulatedPlayerExchangeCards = new ArrayList<>();

        System.out.println("*** Cards in hand ***");
        this.showCards();
        String userInput = "no";
        if (playerCardList.size() > 2) {
            System.out.println("Do you wish to exchange cards ? (yes/no)");
            userInput = input.nextLine();
        } else {
            System.out.println(playerName + " does not have sufficient cards to trade.");
        }
        while (!((userInput.toLowerCase().equals("yes")) || (userInput.toLowerCase().equals("no")))) {
            System.out.println("Please input either yes or no.");
            userInput = input.nextLine();
        }

        while ((userInput.equals("yes")) || (userInput.equals("no") && playerCardList.size() > 4)) {
            if (userInput.equals("no") && (playerCardList.size() > 4)) {
                System.out.println("You must exchange cards. You have more than 4 cards in your hand.");
            }
            boolean can_exchange = false;
            this.showCards();
            Integer cardNumber;

            while (!can_exchange) {
                playerExchangeCards = new ArrayList<Card>();
                System.out
                        .println("Please enter three card numbers from the list of the same or different army types.");
                for (int i = 0; i < 3; i++) {
                    cardNumber = input.nextInt();
                    while (cardNumber >= playerCardList.size()) {
                        System.out.println("Please input correct number from list");
                        cardNumber = input.nextInt();
                    }
                    playerExchangeCards.add(playerCardList.get(cardNumber));
                }
                can_exchange = isValidExchange(playerExchangeCards);
            }
            for (Card card : playerExchangeCards) {
                cumulatedPlayerExchangeCards.add(card);
                playerCardList.remove(card);
            }
            System.out.println("*** Cards in hand ***");
            this.showCards();

            System.out.println("Do you wish to exchange cards ? (yes/no)");
            userInput = input.nextLine();
            while (!((userInput.toLowerCase().equals("yes")) || (userInput.toLowerCase().equals("no")))) {
                System.out.println("Please input either yes or no.");
                userInput = input.nextLine();
            }
        }
        if ((userInput.equals("no")) && (playerCardList.size() > 2) && (cumulatedPlayerExchangeCards.size() == 0)) {
            System.out.println(playerName + " does not wish to exchange cards.");
        }
        return cumulatedPlayerExchangeCards;

    }

    public int calculateTotalReinforcement(ArrayList<Card> playerExchangeCards) {
        int totalReinforcementArmyCount = 0;
        totalReinforcementArmyCount += (reinforcementArmyCountFromCards(playerExchangeCards)
                + calculateReinforcementArmy());

        return totalReinforcementArmyCount;
    }

    private int reinforcementArmyCountFromCards(ArrayList<Card> cumulatedPlayerExchangeCards) {
        ArrayList<Card> playerExchangeCards;
        int countReinforcementFromCardExchange = 0;
        while (!(cumulatedPlayerExchangeCards.isEmpty())) {
            playerExchangeCards = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                playerExchangeCards.add(cumulatedPlayerExchangeCards.get(0));
                cumulatedPlayerExchangeCards.remove(0);

            }
            boolean can_exchange = isValidExchange(playerExchangeCards);
            boolean extraTerritoryMatchArmy = isExtraTerritoryMatchArmy(playerExchangeCards);

            if (can_exchange == true) {
                if (extraTerritoryMatchArmy == true) {
                    countReinforcementFromCardExchange += 2;
                }
                countReinforcementFromCardExchange += Player.getCardExchangeArmyCount();
                Player.setCardExchangeArmyCount();
            }
            for (Card card : playerExchangeCards) {
                removeFromPlayerCardList(card);
                this.gameData.cardsDeck.setCard(card);
            }
        }
        return countReinforcementFromCardExchange;
    }

    public boolean isValidExchange(ArrayList<Card> playerExchangeCards) {
        boolean condition_same = ((playerExchangeCards.get(0).getArmyType()
                .equals(playerExchangeCards.get(1).getArmyType()))
                && playerExchangeCards.get(0).getArmyType().equals(playerExchangeCards.get(2).getArmyType()));
        boolean condition_different = (!(playerExchangeCards.get(0).getArmyType()
                .equals(playerExchangeCards.get(1).getArmyType())))
                && (!(playerExchangeCards.get(0).getArmyType().equals(playerExchangeCards.get(2).getArmyType())))
                && (!(playerExchangeCards.get(1).getArmyType().equals(playerExchangeCards.get(2).getArmyType())));
        return (condition_same || condition_different);
    }

    private boolean isExtraTerritoryMatchArmy(ArrayList<Card> playerExchangeCards) {
        boolean extraTerritoryMatchArmy = false;

        for (Card card : playerExchangeCards) {
            if (playerID == card.getCountry().getCountryConquerorID()) {
                extraTerritoryMatchArmy = true;
                break;
            }
        }
        return extraTerritoryMatchArmy;
    }

    private void placeArmy(Integer reinforcementArmy) {

        Integer currentPlayerID = playerID;
        HashSet<String> countriesOwned = this.gameData.gameMap.getConqueredCountriesPerPlayer(currentPlayerID);

        System.out.println();
        System.out.println("**** Reinforcement Phase Begins for player " + this.playerName + "..****\n");

        while (reinforcementArmy > 0) {

            System.out.print(playerName + "'s Total Reinforcement Army Count Remaining -> ["
                    + String.valueOf(reinforcementArmy) + "]\n");

            /* Information about the countries owned by the player and enemy countries. */
            for (String countries : countriesOwned) {

                System.out.println("\nCountry owned by " + playerName + "-> " + countries + " & Army Count: "
                        + this.gameData.gameMap.getCountry(countries).getCountryArmyCount());

                HashSet<String> adjCountries = this.gameData.gameMap.getAdjacentCountries(countries);

                if (adjCountries.isEmpty()) {
                    System.out.println("No neighboring enemy country for country " + countries);
                }

                for (String enemyCountries : adjCountries) {
                    if (this.gameData.gameMap.getCountry(enemyCountries).getCountryConquerorID() != currentPlayerID) {
                        System.out.println("Neighboring Enemy country name: " + enemyCountries + " & Army Count: "
                                + this.gameData.gameMap.getCountry(enemyCountries).getCountryArmyCount());
                    }
                }
            }

            System.out.println("\nEnter the country name to place armies: ");
            String countryNameByUser = input.nextLine();

            /* Check for an invalid country name. */
            if (this.gameData.gameMap.getCountry(countryNameByUser) == null) {
                System.out.println("'" + countryNameByUser
                        + "' is an invalid country name. Please verify the country name from the list.\n\n");
                continue;
            }

            /*
             * Check for a valid country name, but the country belonging to a different
             * player.
             */

            if (this.gameData.gameMap.getCountry(countryNameByUser).getCountryConquerorID() != currentPlayerID) {
                System.out.println("'" + countryNameByUser
                        + "' does not belong to you yet!!. Please verify your countries owned from the list below.\n\n");
                continue;
            }

            /* Information about armies and placement of armies */
            System.out.println("Enter the number of armies to be placed, Remaining Army (" + reinforcementArmy + ") :");

            try {
                Integer numberOfArmiesToBePlacedByUser = Integer.parseInt(input.nextLine());

                if (numberOfArmiesToBePlacedByUser > reinforcementArmy) {
                    System.out.println("Input value '" + numberOfArmiesToBePlacedByUser
                            + "' should not be greater than the Total Reinforcement Army Count " + "("
                            + String.valueOf(reinforcementArmy) + ")\n\n");
                    continue;
                }

                if (!(numberOfArmiesToBePlacedByUser > 0)) {
                    System.out.println("Please input a value greater than 0.\n\n");
                    continue;
                }

                System.out.println("Successful...Country chosen " + countryNameByUser + " ,Number of armies placed: "
                        + numberOfArmiesToBePlacedByUser + "\n\n");

                this.gameData.gameMap.addArmyToCountry(countryNameByUser, numberOfArmiesToBePlacedByUser);
                reinforcementArmy -= numberOfArmiesToBePlacedByUser;

                ReinforcementPhaseState reinforcementPhase = new ReinforcementPhaseState();
                reinforcementPhase.setToCountry(countryNameByUser);
                reinforcementPhase.setNumberOfArmiesPlaced(numberOfArmiesToBePlacedByUser);
                reinforcementPhaseState.add(reinforcementPhase);


            } catch (NumberFormatException ex) {
                System.out.println(ex.getMessage() + ", please enter numeric values only!\n\n");
                continue;
            }
        }
        /* End of reinforcement phase, Print the final overview. */
        System.out.println("Reinforcement Phase is now complete. Here's an overview: \n\n");
        for (String countries : countriesOwned) {
            System.out.println("Country owned by you: " + countries + " ,Army Count: "
                    + this.gameData.gameMap.getCountry(countries).getCountryArmyCount());
        }
        reinforcementPhaseState.clear();
        System.out.println("\n**** Reinforcement Phase Ends for player " + this.playerName + "..****\n");
    }

    public ArrayList<Card> getPlayerCardList() {
        return cardList;
    }

    private void showCards() {

        ArrayList<Card> playerCardList = getPlayerCardList();
        ArrayList<String> playerCountryList = new ArrayList<String>();

        int cardsCount = 0;

        for (Card cards : playerCardList) {
            System.out.println(cardsCount + ". " + cards.getCountry().getCountryName() + " " + cards.getArmyType());
            playerCountryList.add(cards.getCountry().getCountryName());
            cardsCount++;
        }
    }

    public Integer calculateReinforcementArmy() {

        Integer totalReinforcementArmyCount = 0;
        Integer totalCountriesOwnedByPlayer;
        Integer currentPlayerID = playerID;
        ArrayList<Card> playerCardList = getPlayerCardList();
        ArrayList<Card> playerExchangeCards;
        ArrayList<String> playerCountryList = new ArrayList<String>();
        boolean can_exchange = false;

        /*
         * Count the total number of continents owned by the player and retrieve the
         * continent's control value.
         */
        HashSet<String> conqueredContinentsPerPlayer = this.gameData.gameMap
                .getConqueredContinentsPerPlayer(currentPlayerID);

        for (String continent : conqueredContinentsPerPlayer) {
            Integer controlValue = this.gameData.gameMap.getContinent(continent).getContinentControlValue();
            totalReinforcementArmyCount += controlValue;
        }

        /*
         * Count the total number of countries owned by the player and provide a minimum
         * of three armies.
         */
        totalCountriesOwnedByPlayer = this.gameData.gameMap.getConqueredCountriesPerPlayer(currentPlayerID).size();
        totalReinforcementArmyCount += totalCountriesOwnedByPlayer
                / REINFORCEMENT_DIVISION_FACTOR > MINIMUM_REINFORCEMENT_ARMY_NUMBER
                ? totalCountriesOwnedByPlayer / REINFORCEMENT_DIVISION_FACTOR
                : MINIMUM_REINFORCEMENT_ARMY_NUMBER;
        return totalReinforcementArmyCount;
    }

    public void removeFromPlayerCardList(Card card) {
        this.cardList.remove(card);
    }
}
