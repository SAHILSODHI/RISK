package com.java.model.player;

import com.java.model.cards.Card;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;


public class AggresiveMode extends PlayerStrategy {

    public AggresiveMode(Integer playerID, String playerName) {

        super(playerID,playerName);

    }
    
    private static Scanner input = new Scanner(System.in);


    @Override
    public ArrayList<Card> getValidCards() {
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

                if (!can_exchange) {
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
                    if(!can_exchange){
                        System.out.println("You cannot trade these cards. You should enter three card numbers from the " +
                                "list of the same or different army types.");
                        System.out.println("*** Cards in hand ***");
                        this.showCards();
                        System.out.println("Do you wish to exchange cards ? (yes/no)");
                        userInput = input.nextLine();
                        userInput = input.nextLine();
                        while (!((userInput.toLowerCase().equals("yes")) || (userInput.toLowerCase().equals("no")))) {
                            System.out.println("Please input either yes or no.");
                            userInput = input.nextLine();
                        }
                        continue;
                    }
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

    /**
     * The executeAttack() method encompasses the overall attack phase logic and flow
     * including both single and all-out mode. Attack phase ends when player either
     * no longer wants to attack, or cannot attack.
     */
    @Override
    public void executeReinforcement() {
        notifyView();

        ArrayList<Card> playerExchangeCards;
        playerExchangeCards = getValidCards();
        Integer totalReinforcementArmyCount = calculateTotalReinforcement(playerExchangeCards);

        ReinforcementPhaseState reinforcementPhase = new ReinforcementPhaseState();
        reinforcementPhase.setNumberOfArmiesReceived(totalReinforcementArmyCount);

        reinforcementPhaseState.add(reinforcementPhase);

        notifyView();
        placeArmy(totalReinforcementArmyCount);
    }


    @Override
    public void placeArmy(Integer reinforcementArmy) {

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
            String countryNameByUser = "";
            do{
                System.out.println("Please enter the country name to place armies: ");
                countryNameByUser = input.nextLine();
            }while (countryNameByUser.equals(""));


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
                notifyView();

            } catch (NumberFormatException ex) {
                System.out.println(ex.getMessage() + ", please enter numeric values only!\n\n");
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


    @Override
    public void executeAttack() {
    	
    	System.out.println();
        System.out.println("**** Attack Phase Begins for player " + this.playerName + "..****\n");
        
        boolean canAttack = true;
        boolean hasConnqueredAtleastOneCountry = false;
        
        while(canAttack) {
	        System.out.println("\n" + "Fetching potential attack scenarios for " + this.playerName + "...\n");
	        
	        // get all scenarios but we're only interested in attacking with the strongest country & in all-out mode
	        HashMap<String, ArrayList<String>> potentialAttackScenarios = getPotentialAttackScenarios();
	        
	        if (potentialAttackScenarios == null) {
	            System.out.println(
	                    "There are currently no attack opportunities for " + this.playerName + ".. Sorry!\n");
	            System.out.println("\n****Attack Phase Ends for player " + this.playerName + "..****\n");
	            return;
	        }
	
	        if (potentialAttackScenarios.isEmpty()) {
	            System.out.println(
	                    "There are currently no attack opportunities for " + this.playerName + ".. Sorry!\n");
	            System.out.println("\n****Attack Phase Ends for player " + this.playerName + "..****\n");
	            return;
	        }
	        
	        AttackPhaseState attackPhase = new AttackPhaseState();
	        attackPhase.setAttackingPlayer(this.playerName);
	        attackPhaseState.add(attackPhase);
	        notifyView();
	        
	        // it's not enough to just pick up the strongest country
	        // we need it to have adjacent enemies to attack
	        boolean noAdjacentEnemyCountriesToAttack = true;
	        String strongestCountry = null;
	        String enemyCountryToAttack = null;
	        while (noAdjacentEnemyCountriesToAttack) {
	        	strongestCountry = getStrongestCountryConqueredByPlayer(potentialAttackScenarios);
	            if(strongestCountry == null) {
	                System.out.println(
	                        "There are currently no attack opportunities for " + this.playerName + ".. Sorry!\n");
	                System.out.println("\n****Attack Phase Ends for player " + this.playerName + "..****\n");
	                return;
	            }
	        	HashSet<String> adjacentCountries = gameData.gameMap.getAdjacentCountries(strongestCountry);
	        	for(String adjCountry : adjacentCountries){
	                if(gameData.gameMap.getCountry(adjCountry).getCountryConquerorID() != playerID){
	               		noAdjacentEnemyCountriesToAttack = false;
	               		enemyCountryToAttack = adjCountry;
	               		break;
	                }
	            }
	        	// eliminate strongest country as a possibility before looping back
	        	if(noAdjacentEnemyCountriesToAttack) {
	        		potentialAttackScenarios.remove(strongestCountry);
	        	}      	
	        }
	        
	        if(strongestCountry == null || enemyCountryToAttack == null) {
	            System.out.println(
	                    "There are currently no attack opportunities for " + this.playerName + ".. Sorry!\n");
	            System.out.println("\n****Attack Phase Ends for player " + this.playerName + "..****\n");
	            return;
	        }
	        
	        attackPhase.setAttackingCountry(strongestCountry);
	        notifyView();
	
	        attackPhase.setDefendingCountry(enemyCountryToAttack);
	        notifyView();
	
	        String defendingPlayer = gameData
	                .getPlayer(this.gameData.gameMap.getCountry(enemyCountryToAttack).getCountryConquerorID())
	                .getStrategyType().getPlayerName();
	        attackPhase.setDefendingPlayer(defendingPlayer);
	        notifyView();
	        
	        while (!attackPhase.getBattleOutcomeFlag()) {
                if (this.gameData.gameMap.getCountry(strongestCountry).getCountryArmyCount() > 1) {
                	// proceed with max allowed dice count for both sides
	                Integer attackerDiceCount = getActualMaxAllowedDiceCountForAction("attack", strongestCountry,3);
	                attackPhase.setAttackerDiceCount(attackerDiceCount);
	                Integer defenderDiceCount = getActualMaxAllowedDiceCountForAction("defend",enemyCountryToAttack, 2);
	                attackPhase.setDefenderDiceCount(defenderDiceCount);
	                rollDiceBattle(attackPhase);
                    hasConnqueredAtleastOneCountry = fight(attackPhase) || hasConnqueredAtleastOneCountry;
                }

            checkIfPlayerHasConqueredTheWorld();

            }

	        if (hasConnqueredAtleastOneCountry) {
	        	Card card = gameData.cardsDeck.getCard();
            
	        	if(card == null) {
	        		System.out.println("No more cards left in the deck");
	        	} else {
	        		this.cardList.add(card);
	        		System.out.println("PlayerStrategy received 1 card => Army Type: " + card.getArmyType() + ", Country: " + card.getCountry().getCountryName());
	        		System.out.println("Total cards : " + this.cardList.size());
	        	}
	        }

        
        }
        
        endAttack();
    }

    @Override
    public String getCountryToAttackFrom(HashMap<String, ArrayList<String>> attackScenarios) {
        return null;
    }

    @Override
    public String getEnemyCountryToAttack(String selectedSourceCountry, HashMap<String, ArrayList<String>> attackScenarios) {
        return null;
    }

    @Override
    public Integer getDesiredDiceCountFromPlayer(String player, String country, String action) {
        return null;
    }

    @Override
    public Integer getNumberofArmiesAttackerWantsToMove(String selectedSourceCountry) {
        return null;
    }

    @Override
    public void executeFortification() {
    	
    	System.out.println();
        System.out.println("**** Fortification Phase Begins for player " + this.playerName + "..****\n");

        System.out.println("\n" + "Fetching potential fortification scenarios for " + this.playerName + "...\n");

        HashMap<String, ArrayList<String>> potentialFortificationScenarios = getPotentialFortificationScenarios();
        
        if (potentialFortificationScenarios == null) {
            System.out.println(
                    "There are currently no fortification opportunities for " + this.playerName + ".. Sorry!\n");
            System.out.println("\n****Fortification Phase Ends for player " + this.playerName + "..****\n");
            return;
        }

        // if the list is empty or only contains a single scenario means we don't have anything to check/do 
        // that's because the hashmap of scenarios is keyed on source country to fortify from
        // and if there's only 1 scenario in total, means other countries only have 1 army on the ground and cant help
        if (potentialFortificationScenarios.isEmpty() || potentialFortificationScenarios.size() == 1) {
            System.out.println(
                    "There are currently no 'aggressive' fortification opportunities for " + this.playerName + ".. Sorry!\n");
            System.out.println("\n****Fortification Phase Ends for player " + this.playerName + "..****\n");
            return;
        }
        
        String strongestCountry = getStrongestCountryConqueredByPlayer(potentialFortificationScenarios);
        String secondStrongestCountry = getSecondStrongestCountryConqueredByPlayer(potentialFortificationScenarios,strongestCountry);


        Integer maxNoOfArmiesToMove = gameData.gameMap.getCountry(secondStrongestCountry).getCountryArmyCount() - 1;

        gameData.gameMap.getCountry(secondStrongestCountry).deductArmy(maxNoOfArmiesToMove);
        gameData.gameMap.getCountry(strongestCountry).addArmy(maxNoOfArmiesToMove);
        HashSet<String> conqueredCountryByThisPlayer = gameData.gameMap.getConqueredCountriesPerPlayer(playerID);
        System.out.println("Moved "+maxNoOfArmiesToMove+" armies from "+secondStrongestCountry+" to "+strongestCountry);
        System.out.println("\nAn overview after Fortification.\n");
        for(String country: conqueredCountryByThisPlayer){
            System.out.println("Country: "+country+", Army Count: "+gameData.gameMap.getCountry(country).getCountryArmyCount());
        }

    }
    
    /**	
     * This helper method is utilized by all phases to return,
     * at any given point in time, the strongest country 
     * currently conquered by the player
     * @param potentialScenarios
     * @return
     */
    
    public String getStrongestCountryConqueredByPlayer(HashMap<String, ArrayList<String>> potentialScenarios) {
		
    	String strongestCountry = null;
    	Integer maxArmyCountEncountered = 1;
        
        // find the strongest country conquered by the player, knowing it would be contained as a key 
        for (String country : potentialScenarios.keySet()){
        	Integer currentCountryArmyCount = gameData.gameMap.getCountry(country).getCountryArmyCount();
        	if (currentCountryArmyCount > maxArmyCountEncountered) {
        		strongestCountry = country;
        		maxArmyCountEncountered = currentCountryArmyCount;
        	}        
        }
        
    	return strongestCountry;
    }
    
    public String getSecondStrongestCountryConqueredByPlayer(HashMap<String, ArrayList<String>> potentialScenarios, String strongestCountry) {
		
    	String secondStrongestCountry = null;
    	Integer maxArmyCountEncountered = 1;
        
        // find second strongest country conquered by the player based on value passed in as strongest 
    	for(String country: potentialScenarios.get(strongestCountry)) {
        	Integer currentCountryArmyCount = gameData.gameMap.getCountry(country).getCountryArmyCount();
        	// only countries with 2 or more armies on the ground qualify as suppliers
        	if (currentCountryArmyCount > maxArmyCountEncountered) {
        		secondStrongestCountry = country;
        		maxArmyCountEncountered = currentCountryArmyCount;
        	}
        }
        
    	return secondStrongestCountry;
    }
    
    
    
}
