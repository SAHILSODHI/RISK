package com.java.model.player;

import com.java.controller.dice.Dice;
import com.java.model.Observable;
import com.java.model.cards.Card;
import com.java.model.gamedata.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * This class models the player, it holds the id, the name, and the order to
 * play for the round robin.
 * 
 * @author Arnav Bhardwaj
 * @author Karan Dhingra
 * @author Ghalia Elkerdi
 * @author Sahil Singh Sodhi
 * @author Cristian Rodriguez
 * @version 2.0.0
 */
public class Player extends Observable {

	Reinforce reinforce;
	Attack attack;
	Fortify fortify;
	private Integer playerID;
	private String playerName;
	private Integer orderOfPlay;
	private ArrayList<Card> cardList;
	private static int cardExchangeArmyCount = 5;
	public Boolean isActive = true;
	public Boolean gameOn = true;
	private Boolean isWinner = false;

	private Scanner input;
	private GameData gameData;
	private Dice playerDice;

	private ArrayList<AttackPhaseState> attackPhaseState;
	private ArrayList<ReinforcementPhaseState> reinforcementPhaseState;
	private ArrayList<FortificationPhaseState> fortificationPhaseState;

	private static final int MINIMUM_REINFORCEMENT_ARMY_NUMBER = 3;
	private static final int REINFORCEMENT_DIVISION_FACTOR = 3;

	/**
	 * Creates a player by giving the id and the name
	 * 
	 * @param playerID   the player id.
	 * @param playerName the player name.
	 */
	public Player(Integer playerID, String playerName) {
		this.playerID = playerID;
		this.playerName = playerName;
		this.cardList = new ArrayList<Card>();
		attackPhaseState = new ArrayList<>();
		reinforcementPhaseState = new ArrayList<>();
		fortificationPhaseState = new ArrayList<>();
		this.playerDice = new Dice();
	}

	/**
	 * get player's win status
	 * 
	 * @return true if player is a winner else false
	 */
	public Boolean getIsWinner() {
		return isWinner;
	}

	/**
	 * get player's active status
	 * 
	 * @return true if player is still in the game else false
	 */
	public boolean getIsActive() {
		return isActive;
	}

	/**
	 * set true if player won the game
	 * 
	 * @param isWinner
	 */
	public void setIsWinner(Boolean isWinner) {
		this.isWinner = isWinner;
	}

	/**
	 * The startTurn() method organizes the flow of the game by ordering
	 * phase-execution.
	 */
	public void startTurn() {
		input = new Scanner(System.in);
		startReinforcement();
		startAttack();
		// attack phase changes state so before going to fortify logic, check
		isWinner = checkIfPlayerHasConqueredTheWorld();
		if (!isWinner) {
			fortify();
		} else {
			return;
		}
	}

	/**
	 * Starts the reinforcement phase by getting valid cards and calculating the
	 * number of armies.
	 */
	public void startReinforcement() {

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

	/**
	 * Gets valid cards to trade.
	 * 
	 * @return array of valid cards.
	 */
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

	/**
	 * PlaceArmy method allows the player to position the armies in the player's
	 * owned countries.
	 * 
	 * @param reinforcementArmy total reinforcement army count to be placed by the
	 *                          current player.
	 */
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
				notifyView();

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

	/**
	 * Displays the cards.
	 */
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

	/**
	 * Calculates the total number of armies to be given during reinforcement.
	 * 
	 * @param playerExchangeCards the cards that the player exchanged
	 * @return The total number of reinforcement army for the player(integer value).
	 */
	public int calculateTotalReinforcement(ArrayList<Card> playerExchangeCards) {
		int totalReinforcementArmyCount = 0;
		totalReinforcementArmyCount += (reinforcementArmyCountFromCards(playerExchangeCards)
				+ calculateReinforcementArmy());

		return totalReinforcementArmyCount;
	}

	/**
	 * Function to count the reinforcement army based on the number of territories
	 * and continents owned.
	 * 
	 * @return total reinforcement army count
	 */
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

	/**
	 * Calculates the reinforcement army from card exchange.
	 * 
	 * @param cumulatedPlayerExchangeCards the cards that the player holds.
	 * @return the number of reinforcement army from card exchange.
	 * 
	 */
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

	/**
	 * If a player has card with a country that he owns he will receive more armies.
	 * 
	 * @param playerExchangeCards cards to be exchanged
	 * @return true if the player deserves more armies.
	 */
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

	/***
	 * Helper method to check if it is valid card exchange.
	 * 
	 * @param playerExchangeCards cards to exchange.
	 * @return true if the exchange is valid.
	 */
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

	/**
	 * The startAttack() method encompasses the overall attack phase logic and flow
	 * including both single and all-out mode. Attack phase ends when player either
	 * no longer wants to attack, or cannot attack.
	 */

	private void startAttack() {

		System.out.println();
		System.out.println("**** Attack Phase Begins for player " + this.playerName + "..****\n");

		// implement an all-out mode
		boolean allOut = false;
		Boolean hasConnqueredAtleastOneCountry = false;

		while (gameOn) {

			AttackPhaseState attackPhase = new AttackPhaseState();
			attackPhase.setAttackingPlayer(this.playerName);
			attackPhaseState.add(attackPhase);
			notifyView();

			boolean wantToAttack = false;
			String playerDecision = "no";

			// First get confirmation from the player that attack is desired
			// unless player had specified "all out" mode

			System.out.println("\n Would you like to attack? (YES/NO)");
			if (input.hasNextLine()) {
				playerDecision = input.nextLine();
			}

			switch (playerDecision.toLowerCase()) {
			case "yes":
				wantToAttack = true;
				break;
			case "yeah":
				wantToAttack = true;
				break;
			case "y":
				wantToAttack = true;
				break;
			case "sure":
				wantToAttack = true;
				break;
			}

			if (!wantToAttack) {
				System.out.println("\n" + this.playerName + "does not wish to attack..");
				attackPhaseState.clear();
				notifyView();
				break;
			}

			// Now fetch all attack possibilities for player
			System.out.println("\n" + "Fetching potential attack scenarios for " + this.playerName + "...\n");
			HashMap<String, ArrayList<String>> attackScenarios = getPotentialAttackScenarios();

			if (attackScenarios.isEmpty()) {
				System.out
						.println("There are currently no attack opportunities for " + this.playerName + ".. Sorry!\n");
				break;
			}

			// show Player all the options they have
			showAllAttackScenarios(attackScenarios);
			String selectedSourceCountry = getCountryToAttackFrom(attackScenarios);
			attackPhase.setAttackingCountry(selectedSourceCountry);
			notifyView();

			String selectedDestinationCountry = getEnemyCountryToAttack(selectedSourceCountry, attackScenarios);
			attackPhase.setDefendingCountry(selectedDestinationCountry);
			notifyView();

			String defendingPlayer = gameData
					.getPlayer(this.gameData.gameMap.getCountry(selectedDestinationCountry).getCountryConquerorID())
					.getPlayerName();
			attackPhase.setDefendingPlayer(defendingPlayer);
			notifyView();

			// Check if attacking player wants to "go all out"
			if (!allOut) {
				System.out.println("\n Would you like to go all out? (YES/NO)");
				if (input.hasNextLine()) {
					playerDecision = input.nextLine();
				}

				switch (playerDecision.toLowerCase()) {
				case "yes":
					allOut = true;
					break;
				case "yeah":
					allOut = true;
					break;
				case "y":
					allOut = true;
					break;
				case "sure":
					allOut = true;
					break;
				}
			}

			// Based on what mode the attack is set to happen in, these will be determined
			// differently
			Integer selectedAttackerDiceCount = 1;
			Integer selectedDefenderDiceCount = 1;

			// attack once
			if (!allOut) {
				// prompt attacker and defender for dice count preferences
				selectedAttackerDiceCount = getDesiredDiceCountFromPlayer(this.playerName, selectedSourceCountry,
						"attack");
				attackPhase.setAttackerDiceCount(selectedAttackerDiceCount);
				notifyView();

				selectedDefenderDiceCount = getDesiredDiceCountFromPlayer(defendingPlayer, selectedDestinationCountry,
						"defend");
				attackPhase.setDefenderDiceCount(selectedDefenderDiceCount);
				notifyView();

				rollDiceBattle(attackPhase);

				hasConnqueredAtleastOneCountry = fight(attackPhase) || hasConnqueredAtleastOneCountry;
			}

			// or keep attacking if all-out mode & player still can attack & player still
			// hasn't conquered target
			while (allOut && !attackPhase.getBattleOutcomeFlag()) {
				if (this.gameData.gameMap.getCountry(selectedSourceCountry).getCountryArmyCount() > 1) {
					// dont prompt players for input, proceed with max allowed dice count for both
					// players
					selectedAttackerDiceCount = getActualMaxAllowedDiceCountForAction("attack", selectedSourceCountry,
							3);
					attackPhase.setAttackerDiceCount(selectedAttackerDiceCount);
					selectedDefenderDiceCount = getActualMaxAllowedDiceCountForAction("defend",
							selectedDestinationCountry, 2);
					attackPhase.setDefenderDiceCount(selectedDefenderDiceCount);
					rollDiceBattle(attackPhase);
					hasConnqueredAtleastOneCountry = fight(attackPhase) || hasConnqueredAtleastOneCountry;
				} else {
					break;
				}
			}
			allOut = false;

			checkIfPlayerHasConqueredTheWorld();

		}
		
		if (hasConnqueredAtleastOneCountry) {
			Card card = gameData.cardsDeck.getCard();
			this.cardList.add(card);
			System.out.println("Player received 1 card => Army Type: " + card.getArmyType() + ", Country: " + card.getCountry().getCountryName());
			System.out.println("Total cards : " + this.cardList.size());
		}
		
		endAttack();
	}

	/**
	 * rollDiceBattle resorts to the core rollDice method in Dice but contains
	 * additional logic to set attackPhase logic
	 * 
	 * @param attackPhase has all the dice count info for attacker and defender
	 */

	private void rollDiceBattle(AttackPhaseState attackPhase) {

		System.out.println("\n ROLLING DICE \n");

		Integer selectedDefenderDiceCount = attackPhase.getDefenderDiceCount();
		Integer selectedAttackerDiceCount = attackPhase.getAttackerDiceCount();

		ArrayList<Integer> attackerDiceRolls = playerDice.rollDice(selectedAttackerDiceCount);
		ArrayList<Integer> defenderDiceRolls = playerDice.rollDice(selectedDefenderDiceCount);

		attackPhase.setAttackerDiceRollResults(attackerDiceRolls);
		attackPhase.setDefenderDiceRollResults(defenderDiceRolls);

		System.out.println("\nAttacker rolled: " + attackerDiceRolls.toString());
		System.out.println("\nDefender rolled: " + defenderDiceRolls.toString());

	}

	/**
	 * All of the potential attack scenarios for current player.
	 * 
	 * @return the comprehensive list with all source and target countries.
	 */

	public HashMap<String, ArrayList<String>> getPotentialAttackScenarios() {

		HashMap<String, ArrayList<String>> attackScenarios = new HashMap<String, ArrayList<String>>();
		// source countries which Player could attack FROM
		HashSet<String> poolOfPotentialSourceCountries = this.gameData.gameMap.getConqueredCountriesPerPlayer(playerID);

		// for build 3 strategies
		// HashSet<String> poolOfPotentialDestinationCountries = new HashSet<String>();
		// HashSet<String> destinationCountriesWithMultiSharedBorders = new
		// HashSet<String>();

		for (String potentialSourceCountry : poolOfPotentialSourceCountries) {
			// eliminate countries with only a single army count
			if (this.gameData.gameMap.getCountry(potentialSourceCountry).getCountryArmyCount() < 2) {
				continue;
			}
			HashSet<String> adjacentCountries = new HashSet<String>();
			adjacentCountries = this.gameData.gameMap.getAdjacentCountries(potentialSourceCountry);
			for (String adjacentCountry : adjacentCountries) {
				// ensure adjacent country not owned by same player
				if (this.gameData.gameMap.getCountry(adjacentCountry).getCountryConquerorID() != playerID) {
					/*
					 * if(poolOfPotentialDestinationCountries.contains(adjacentCountry)) { // this
					 * enemy country can be attacked from multiple fronts
					 * if(!destinationCountriesWithMultiSharedBorders.contains(adjacentCountry)) {
					 * destinationCountriesWithMultiSharedBorders.add(adjacentCountry); } } else {
					 * poolOfPotentialDestinationCountries.add(adjacentCountry); }
					 */ // for build 3
					attackScenarios.putIfAbsent(potentialSourceCountry, new ArrayList<String>());
					attackScenarios.get(potentialSourceCountry).add(adjacentCountry);
				}
			}
		}

		return attackScenarios;
	}

	/**
	 * Helper method to print out all of the potential attack scenarios for current
	 * player.
	 * 
	 * @param attackScenarios: the comprehensive list with all source and target
	 *        countries.
	 * 
	 */
	private void showAllAttackScenarios(HashMap<String, ArrayList<String>> attackScenarios) {

		HashMap<String, Integer> maxAttackArmyCountPossiblePerSrcCountry = new HashMap<String, Integer>();
		HashMap<String, Integer> maxDefenseArmyCountPossiblePerDestCountry = new HashMap<String, Integer>();

		// Print all the options out for the player to see and choose from
		for (String keySourceCountry : attackScenarios.keySet()) {
			int actualArmyCount = this.gameData.gameMap.getCountry(keySourceCountry).getCountryArmyCount();
			int maxAttackArmyCount = actualArmyCount - 1;
			if (maxAttackArmyCount >= 3) {
				maxAttackArmyCountPossiblePerSrcCountry.put(keySourceCountry, 3);
			} else if (maxAttackArmyCount >= 2) {
				maxAttackArmyCountPossiblePerSrcCountry.put(keySourceCountry, 2);
			} else {
				maxAttackArmyCountPossiblePerSrcCountry.put(keySourceCountry, 1);
			}
			for (String correspondingDestinationCountry : attackScenarios.get(keySourceCountry)) {
				int defenderArmyCount = this.gameData.gameMap.getCountry(correspondingDestinationCountry)
						.getCountryArmyCount();
				int defenderMaxDiceCount = 1;
				if (defenderArmyCount >= 2) {
					defenderMaxDiceCount = 2;
				}
				maxDefenseArmyCountPossiblePerDestCountry.putIfAbsent(correspondingDestinationCountry,
						defenderMaxDiceCount);
				System.out.println("\n" + keySourceCountry + " (occupied by " + actualArmyCount + " of your armies)"
						+ "\t -> \t" + correspondingDestinationCountry + "\t **defended by " + defenderArmyCount
						+ " of "
						+ gameData.getPlayer(this.gameData.gameMap.getCountry(correspondingDestinationCountry)
								.getCountryConquerorID()).getPlayerName()
						+ "'s armies**" + "\t (attack with up to "
						+ maxAttackArmyCountPossiblePerSrcCountry.get(keySourceCountry) + " armies)");
			}
		}
	}

	/**
	 * Helper method to get and also validate the source country player wishes to
	 * attack from.
	 * 
	 * @param attackScenarios: the comprehensive list with all source and target
	 *        countries.
	 * @return sourceCountry: a validated country option to attack from.
	 * 
	 */
	private String getCountryToAttackFrom(HashMap<String, ArrayList<String>> attackScenarios) {

		String selectedSourceCountry = " ";

		do {
			System.out.println("Which one of your countries would you like to attack FROM?");
			if (input.hasNextLine()) {
				selectedSourceCountry = input.nextLine();
			}
		} while (!attackScenarios.containsKey(selectedSourceCountry));

		return selectedSourceCountry;
	}

	/**
	 * Helper method to get and also validate the target country to be attacked.
	 * 
	 * @param attackScenarios: the comprehensive list with all source and target
	 *        countries.
	 * @param selectedSourceCountry: a validated country option to attack from.
	 * @return destinationCountry: a validated target country based on the source.
	 * 
	 */
	private String getEnemyCountryToAttack(String selectedSourceCountry,
			HashMap<String, ArrayList<String>> attackScenarios) {

		String selectedDestinationCountry = " ";

		do {
			System.out.println("\n Which enemy country would you like to attack from " + selectedSourceCountry + "?");
			if (input.hasNextLine()) {
				selectedDestinationCountry = input.nextLine();
			}
		} while (!attackScenarios.get(selectedSourceCountry).contains(selectedDestinationCountry));

		return selectedDestinationCountry;
	}

	/**
	 * Helper method to get and also validate the attacker and defender's dice
	 * selections. This is meant to be a generic functionality which tailors to both
	 * actions.
	 * 
	 * @param player: the player rolling the dice.
	 * @param country: either the source country to attack from / the target country
	 *        to be attacked
	 * @param action: either attack/defense.
	 * @return selectedDiceCount: a valid number of dice player can roll.
	 * 
	 */
	private Integer getDesiredDiceCountFromPlayer(String player, String country, String action) {

		String selectedDiceCount = "1";

		// max # of dice a defender can roll is 2
		int defaultMaxDiceCountAllowedForAction = 2;

		// max # of dice an attacker can roll is 3
		if (action.equals("attack")) {
			defaultMaxDiceCountAllowedForAction = 3;
		}

		// but as countries are attacked, army counts changes and we need to dynamically
		// update thresholds
		int maxDiceCountAllowedForAction = getActualMaxAllowedDiceCountForAction(action, country,
				defaultMaxDiceCountAllowedForAction);

		do {
			System.out.println("How many dice would " + player + " like to roll to " + action + "?" + "\t (up to "
					+ maxDiceCountAllowedForAction + " dice)\n");
			if (input.hasNextLine()) {
				selectedDiceCount = input.nextLine();
			}
		} while (isNaN(selectedDiceCount) || Integer.parseInt(selectedDiceCount) < 1
				|| Integer.parseInt(selectedDiceCount) > maxDiceCountAllowedForAction);

		return Integer.parseInt(selectedDiceCount);
	}

	/**
	 * Helper method to get max allowable dice count given specific context: action
	 * and country army count.
	 * 
	 * @param action: either attack/defense.
	 * @param countryInScopeForAction: either the source country to attack from /
	 *        the target country to be attacked.
	 * @param maxDiceCountAllowedForAction: a default max based on Risk game rules.
	 * @return selectedDiceCount: a valid number of dice to roll based on action and
	 *         army counts.
	 * 
	 */
	public int getActualMaxAllowedDiceCountForAction(String action, String countryInScopeForAction,
			int maxDiceCountAllowedForAction) {

		int countryArmyCount = this.gameData.gameMap.getCountry(countryInScopeForAction).getCountryArmyCount();

		// attacker must have at least 1 army on the ground at all times
		if (action.equals("attack")) {
			countryArmyCount--;
		}

		if (countryArmyCount == 1 || countryArmyCount == 2) {
			maxDiceCountAllowedForAction = countryArmyCount;
		}

		return maxDiceCountAllowedForAction;
	}

	/**
	 * Main fight method which encompasses the dynamic interactions of rolling &
	 * comparing dice This handles the army count updates as well as conqueror id's
	 * if need be.
	 * 
	 * @param attackPhase
	 * @return true if attacker conquers the target country
	 */

	public Boolean fight(AttackPhaseState attackPhase) {

		String selectedSourceCountry = attackPhase.getAttackingCountry();
		String selectedDestinationCountry = attackPhase.getDefendingCountry();

		System.out.println("\n HEADS-UP " + attackPhase.getDefendingPlayer() + " YOU ARE UNDER ATTACK!");

		Integer attackerLostArmyCount = 0;
		Integer defenderLostArmyCount = 0;
		Boolean battleOutcomeFlag = false;

		Integer selectedDefenderDiceCount = attackPhase.getDefenderDiceCount();
		Integer selectedAttackerDiceCount = attackPhase.getAttackerDiceCount();
		notifyView();

		ArrayList<Integer> attackerDiceRolls = attackPhase.getAttackerDiceRollResults();
		ArrayList<Integer> defenderDiceRolls = attackPhase.getDefenderDiceRollResults();
		notifyView();

		// take the lowest dice count among the two
		int benchDiceRoll = java.lang.Math.min(selectedDefenderDiceCount, selectedAttackerDiceCount);

		for (int d = 0; d < benchDiceRoll; d++) {
			if (defenderDiceRolls.get(d) >= attackerDiceRolls.get(d)) {
				System.out.println("\n Attacker loses 1 army count\n");
				this.gameData.gameMap.deductArmyToCountry(selectedSourceCountry, 1);
				attackerLostArmyCount++;
			} else {
				System.out.println("\n Defender loses 1 army count\n");
				this.gameData.gameMap.deductArmyToCountry(selectedDestinationCountry, 1);
				defenderLostArmyCount++;
			}
			
			// if attacker wins
			if (this.gameData.gameMap.getCountry(selectedDestinationCountry).getCountryArmyCount() == 0) {
				// declare new winner
				Integer defenderPlayerId = this.gameData.gameMap.getCountry(selectedDestinationCountry).getCountryConquerorID();
				
				battleOutcomeFlag = true;
				System.out.println("\n" + this.playerName + " has conquered " + selectedDestinationCountry + "!");
				// if game is over, no need to worry about army counts
				if (!checkIfPlayerHasConqueredTheWorld()) {
					selectedAttackerDiceCount = getNumberofArmiesAttackerWantsToMove(selectedSourceCountry);
				}

				this.gameData.gameMap.updateCountryConquerer(selectedDestinationCountry,
						this.gameData.gameMap.getCountry(selectedDestinationCountry).getCountryConquerorID(),
						this.playerID);
				this.gameData.gameMap.deductArmyToCountry(selectedSourceCountry, selectedAttackerDiceCount);
				this.gameData.gameMap.deductArmyToCountry(selectedDestinationCountry, defenderLostArmyCount);
				this.gameData.gameMap.addArmyToCountry(selectedDestinationCountry, selectedAttackerDiceCount);
				
				// hand over defender's cards to attacker if the defender no longer controls any
				// country in the world
				if (checkIfPlayerLostTheGame(defenderPlayerId)) {
					ArrayList<Card> defendersCards = gameData.getPlayer(defenderPlayerId).cardList;
					
					this.cardList.addAll(defendersCards);
					
					System.out.println("Player receives defender's " + defendersCards.size() + " cards");
					// clear defender's cardList
					gameData.getPlayer(defenderPlayerId).cardList.clear();
				}
			}
		}

		attackPhase.setBattleOutcomeFlag(battleOutcomeFlag);
		attackPhase.setAttackerLostArmyCount(attackerLostArmyCount);
		attackPhase.setDefenderLostArmyCount(defenderLostArmyCount);
		// attackPhaseState.add(attackPhase);
		notifyView();

		System.out.println("Army count for " + selectedSourceCountry + " is now: "
				+ this.gameData.gameMap.getCountry(selectedSourceCountry).getCountryArmyCount());
		System.out.println("Army count for " + selectedDestinationCountry + " is now: "
				+ this.gameData.gameMap.getCountry(selectedDestinationCountry).getCountryArmyCount());

		return battleOutcomeFlag;
	}

	/**
	 * Obtain the number of armies that the attcker choses to move to the conqured
	 * country
	 * 
	 * @param selectedSourceCountry pass the value they decide to move to
	 * @return value to move the amount of army
	 */
	private Integer getNumberofArmiesAttackerWantsToMove(String selectedSourceCountry) {

		String selectedArmyCount = "1";
		int maxArmyCountAllowed = this.gameData.gameMap.getCountry(selectedSourceCountry).getCountryArmyCount() - 1;

		do {
			System.out.println("How many armies would " + this.playerName + " like to move to conquered country?"
					+ "\t (up to " + maxArmyCountAllowed + " armies)\n");
			if (input.hasNextLine()) {
				selectedArmyCount = input.nextLine();
			}
		} while (isNaN(selectedArmyCount) || Integer.parseInt(selectedArmyCount) < 1
				|| Integer.parseInt(selectedArmyCount) > maxArmyCountAllowed);

		return Integer.parseInt(selectedArmyCount);
	}

	/**
	 * Helper method called upon when player cannot attack anymore or when player
	 * does not wish to attack anymore.
	 */
	private void endAttack() {

		gameOn = true;
		System.out.println("\n****Attack Phase Ends for player " + this.playerName + "..****\n");
		// attackPhaseState.clear();
		// notifyView();
	}

	/**
	 * Helper method to check if the player conquered the whole world.
	 * 
	 * @return true if they conquered the entire world
	 */
	public boolean checkIfPlayerHasConqueredTheWorld() {

		// boolean isWinner = false;
		HashSet<String> allConqueredCountries = new HashSet<String>();
		allConqueredCountries = this.gameData.gameMap.getConqueredCountriesPerPlayer(this.playerID);

		if (allConqueredCountries.size() == this.gameData.gameMap.getNumberOfCountries()) {
			isWinner = true;
			gameOn = false;
		}
		return isWinner;
	}
	
	/**
	 * Helper method to check if the player lost the game.
	 * @param player pass the value of the player that is in game
	 * @return true if the player is active
	 */
	private boolean checkIfPlayerLostTheGame(Integer playerId) {
		
		HashSet<String> allConqueredCountries = new HashSet<String>();
		allConqueredCountries = this.gameData.gameMap.getConqueredCountriesPerPlayer(playerId);

		if(allConqueredCountries.size() == 0) {
			return true;
		}
		
		return false;
	}

	/**
	 * Method to guide the player through various fortification options when
	 * applicable.
	 */
	public void fortify() {

		System.out.println();
		System.out.println("**** Fortification Phase Begins for player " + this.playerName + "..****\n");

		FortificationPhaseState fortificationPhase = new FortificationPhaseState();
		fortificationPhaseState.add(fortificationPhase);
		notifyView();

		// First get confirmation from the player that fortification is desired.

		boolean doFortify = false;
		String playerDecision = "no";
		System.out.println("Would you like to fortify? (YES/NO)");
		if (input.hasNextLine()) {
			playerDecision = input.nextLine();
		}

		switch (playerDecision.toLowerCase()) {
		case "yes":
			doFortify = true;
			break;
		case "yeah":
			doFortify = true;
			break;
		case "y":
			doFortify = true;
			break;
		case "sure":
			doFortify = true;
			break;
		}

		if (!doFortify) {
			System.out.println(this.playerName + " does not wish to fortify. Ending turn..");
			System.out.println("\n****Fortification Phase Ends for player " + this.playerName + "..****\n");
			return;
		} else {
			System.out.println("\n" + "Fetching potential fortification scenarios for " + this.playerName + "...\n");
		}

		// Now fetch all possibilities for player (this could get long as the game
		// progresses and more land is acquired)

		HashMap<String, ArrayList<String>> fortificationScenarios = getPotentialFortificationScenarios();

		if (fortificationScenarios == null) {
			System.out.println(
					"There are currently no fortification opportunities for " + this.playerName + ".. Sorry!\n");
			System.out.println("\n****Fortification Phase Ends for player " + this.playerName + "..****\n");
			return;
		}

		if (fortificationScenarios.isEmpty()) {
			System.out.println(
					"There are currently no fortification opportunities for " + this.playerName + ".. Sorry!\n");
			System.out.println("\n****Fortification Phase Ends for player " + this.playerName + "..****\n");
			return;
		}

		// This structure will accelerate and organize the army count process/validation
		HashMap<String, Integer> armiesPerPotentialFortificationSourceCountry = new HashMap<String, Integer>();

		// Print all the options out for the player to see and choose from
		for (String keySourceCountry : fortificationScenarios.keySet()) {
			armiesPerPotentialFortificationSourceCountry.put(keySourceCountry,
					this.gameData.gameMap.getCountry(keySourceCountry).getCountryArmyCount());
			// the range is one less because of the minimum requirement of having at least 1
			// army on the ground at all times.
			int possibleNumOfArmyRange = armiesPerPotentialFortificationSourceCountry.get(keySourceCountry) - 1;
			for (String correspondingDestinationCountry : fortificationScenarios.get(keySourceCountry)) {
				if (!correspondingDestinationCountry.equalsIgnoreCase(keySourceCountry)) {
					System.out.println("\n" + keySourceCountry + "\t -> \t" + correspondingDestinationCountry
							+ "\t (up to " + possibleNumOfArmyRange + " armies)");
				}
			}
		}

		// Recycle variable
		// clear the decision variable holder between choices
		playerDecision = "";

		// while selection doesn't match any of the offered options, prompt user
		while (!fortificationScenarios.containsKey(playerDecision)) {
			System.out.println("\n" + "Please choose one of the suggested countries to move armies FROM: ");
			playerDecision = input.nextLine();
		}
		String fromCountry = playerDecision;
		fortificationPhase.setFromCountry(fromCountry);
		notifyView();
		// while number of armies to be moved is not coherent, prompt user
		// 0 is a valid selection
		String noOfArmiesToMove = "-1";
		do {
			System.out.println("\n" + "How many armies would you like to move from " + fromCountry + "?");
			noOfArmiesToMove = input.nextLine();
		} while (isNaN(noOfArmiesToMove) || Integer.parseInt(noOfArmiesToMove) < 0
				|| Integer.parseInt(noOfArmiesToMove) >= armiesPerPotentialFortificationSourceCountry.get(fromCountry));

		fortificationPhase.setNumberOfArmiesMoved(Integer.parseInt(noOfArmiesToMove));
		notifyView();

		playerDecision = "";

		// check that the {from - to} combination specifically makes sense as a valid
		// path
		while (!fortificationScenarios.get(fromCountry).contains(playerDecision)) {
			System.out.println("\n"
					+ "Please choose one of the valid countries to move armies INTO (knowing that you've chosen to move them from country "
					+ fromCountry + "): ");
			playerDecision = input.nextLine();
		}

		String toCountry = playerDecision;

		fortificationPhase.setToCountry(toCountry);
		notifyView();

		// At this stage all that's left to do really is adjust the army counts in the
		// respective countries to reflect they player's fortification move
		this.gameData.gameMap.getCountry(fromCountry).deductArmy(Integer.parseInt(noOfArmiesToMove));
		this.gameData.gameMap.addArmyToCountry(toCountry, Integer.parseInt(noOfArmiesToMove));

		System.out.println(
				"\nFortification Successful for " + this.playerName + ". Here is a summary of the new status-quo:\n");

		System.out.println("Army count for " + fromCountry + " is now: "
				+ this.gameData.gameMap.getCountry(fromCountry).getCountryArmyCount());
		System.out.println("Army count for " + toCountry + " is now: "
				+ this.gameData.gameMap.getCountry(toCountry).getCountryArmyCount());

		fortificationPhaseState.clear();
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("\n****Fortification Phase Ends for player " + this.playerName + "..****\n");
	}

	/**
	 * Helper method to build a comprehensive map of all the possible fortification
	 * paths (for both immediate and extended neighbors) It has all the necessary
	 * checks and validation to ensure the path includes only countries owned by the
	 * given current player in the turn. Moreover, ensures that candidates suggested
	 * as "source countries" to move armies from satisfy the minimal requirements of
	 * army presence on the ground.
	 *
	 * @return Hashmap of all potential fortification scenarios for player.
	 */
	public HashMap<String, ArrayList<String>> getPotentialFortificationScenarios() {

		// Draft/prelim structure which contains only directly adjacent countries owned
		// by the player
		HashMap<String, ArrayList<String>> prelimFortificationScenarios = new HashMap<String, ArrayList<String>>();
		// What will be returned: includes full (extended) paths of countries owned by
		// the player
		HashMap<String, ArrayList<String>> allFortificationScenarios = new HashMap<String, ArrayList<String>>();

		// Step 1: get the comprehensive list of all countries currently conquered by
		// the player
		HashSet<String> poolOfPotentialCountries = new HashSet<String>();
		poolOfPotentialCountries = this.gameData.gameMap.getConqueredCountriesPerPlayer(playerID);

		// Step 2: draw preliminary paths - irrespective of army counts & extended
		// neighbors
		for (String potentialSourceCountry : poolOfPotentialCountries) {
			buildFortificationPath(prelimFortificationScenarios, potentialSourceCountry);
		}

		if (prelimFortificationScenarios.isEmpty()) {
			return null;
		}

		// Before we return the set, we have to slightly manipulate it
		// to ensure the only keys featured are valid source countries which meet the
		// min requirements (at least 2)
		for (String keySourceCountry : prelimFortificationScenarios.keySet()) {
			if (this.gameData.gameMap.getCountry(keySourceCountry).getCountryArmyCount() < 2) {
				continue;
			}
			for (String correspondingDestinationCountry : prelimFortificationScenarios.get(keySourceCountry)) {
				allFortificationScenarios.putIfAbsent(keySourceCountry, new ArrayList<String>());
				allFortificationScenarios.get(keySourceCountry).add(correspondingDestinationCountry);
			}
		}

		// in case all countries conquered by player have 1 army count
		if (allFortificationScenarios.isEmpty()) {
			return null;
		}

		return allFortificationScenarios;
	}

	/**
	 * Small helper method to ensure countries in scope are recursively traversed
	 * and included in the "path" This is NOT the final and full picture for
	 * fortification scenarios, it is merely a stepping stone. The main
	 * getPotentialFortificationScenarios method eliminates the invalid source
	 * candidates based on army counts
	 *
	 * @param fortificationScenarios a HashStructure to be populated
	 * @param rootCountry            country to be checked for adjacency
	 */
	public void buildFortificationPath(HashMap<String, ArrayList<String>> fortificationScenarios, String rootCountry) {
		ArrayList<String> longestConqueredPathFromRoot = new ArrayList<String>();
		longestConqueredPathFromRoot.add(rootCountry);
		traverseNeighbouringCountries(longestConqueredPathFromRoot, rootCountry);
		fortificationScenarios.put(rootCountry, longestConqueredPathFromRoot);
	}

	/**
	 * Small helper method to recursively traverse all countries owned by player and
	 * build a steady path starting from the root country passed in.
	 * 
	 *
	 * @param longestConqueredPathFromRoot to be drawn based on adjacency and
	 *                                     country ownership
	 * @param rootCountry                  country to serve as root of search
	 */
	public void traverseNeighbouringCountries(ArrayList<String> longestConqueredPathFromRoot, String rootCountry) {

		HashSet<String> adjacentCountries = new HashSet<String>();
		adjacentCountries = this.gameData.gameMap.getAdjacentCountries(rootCountry);
		for (String adjacentCountry : adjacentCountries) {
			// ensure adjacent country also owned by same player - otherwise no path
			// to/through it
			if (this.gameData.gameMap.getCountry(adjacentCountry).getCountryConquerorID() == playerID
					&& !longestConqueredPathFromRoot.contains(adjacentCountry)) {
				longestConqueredPathFromRoot.add(adjacentCountry);
				traverseNeighbouringCountries(longestConqueredPathFromRoot, adjacentCountry);
			}
		}
	}

	/**
	 * Getter of the player name.
	 * 
	 * @return the player name.
	 */
	public String getPlayerName() {
		return this.playerName;
	}

	/**
	 * Getter of the player id.
	 * 
	 * @return the player id.
	 */
	public Integer getPlayerID() {
		return this.playerID;
	}

	/**
	 * Getter of the order to play.
	 * 
	 * @return the order of playe.
	 */
	public Integer getOrderOfPlay() {
		return orderOfPlay;
	}

	/**
	 * Setter of the order of play.
	 * 
	 * @param orderOfPlay order of play.
	 */
	public void setOrderOfPlay(Integer orderOfPlay) {
		this.orderOfPlay = orderOfPlay;
	}

	/**
	 * Setter for card exchange army count.
	 */
	public static void setCardExchangeArmyCount() {
		Player.cardExchangeArmyCount += 5;
	}

	/**
	 * Getter for cardExchangeArmyCount which holds the card exchange army count.
	 * 
	 * @return the card exchange army count.
	 */
	public static int getCardExchangeArmyCount() {
		return cardExchangeArmyCount;
	}

	/**
	 * Getter for cardList.
	 * 
	 * @return the card list
	 */
	public ArrayList<Card> getPlayerCardList() {
		return cardList;
	}

	/**
	 * Adds a player to a card list.
	 * 
	 * @param card to be added.
	 */
	public void addToPlayerCardList(Card card) {
		this.cardList.add(card);
	}

	/**
	 * Removes the player from the card list.
	 * 
	 * @param card to be removed.
	 */
	public void removeFromPlayerCardList(Card card) {
		this.cardList.remove(card);
	}

	/**
	 * Acts as previous turn constructor to allow acess to the gamedata's data
	 * 
	 * @param gamedata gamedata objecto to set.
	 */
	public void setGameData(GameData gamedata) {
		this.gameData = gamedata;
	}

	/**
	 * Helper method to test if a given strin can be converted to a int.
	 * 
	 * @param stringInput determines if the string typed by user is an integer
	 * @return the evaluation of true if it is an integer or false otherwise
	 */
	private boolean isNaN(final String stringInput) {
		try {
			Integer.parseInt(stringInput);

		} catch (final Exception e) {

			System.out.println("Invalid Input");
			return true;
		}
		return false;
	}

	/**
	 * Getter for Attack Phase State to show in the phase view.
	 * 
	 * @return the attack phase states to be showed in phase view.
	 */
	public ArrayList<AttackPhaseState> getAttackPhaseState() {
		return attackPhaseState;
	}

	/**
	 * Getter for Reinforcement Phase State to show in the phase view.
	 * 
	 * @return the reinforcement phase states to be showed in phase view.
	 */
	public ArrayList<ReinforcementPhaseState> getReinforcementPhaseState() {
		return reinforcementPhaseState;
	}

	/**
	 * Getter for Fortification Phase State to show in the phase view.
	 * 
	 * @return the Fortification phase states to be showed in phase view.
	 */
	public ArrayList<FortificationPhaseState> getFortificationPhaseState() {
		return fortificationPhaseState;
	}

}
