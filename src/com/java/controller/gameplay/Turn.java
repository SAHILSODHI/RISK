package com.java.controller.gameplay;

import com.java.controller.dice.Dice;
import com.java.model.gamedata.GameData;
import com.java.model.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class Turn implements ReinforcementPhase, AttackPhase, FortificationPhase{

	public GameData gameData;
	public Player player;
	public Integer currentPlayerID;
	private Dice dice;
	private static final int MINIMUM_REINFORCEMENT_ARMY_NUMBER = 3;
	private static final int REINFORCEMENT_DIVISION_FACTOR = 3;

	/*Colors for console output. */
	private static final String ANSI_RESET = "\u001B[0m";
	private static final String ANSI_RED = "\u001B[31m";
	private static final String ANSI_BOLD = "\033[0;1m";

	public Turn(Player player, GameData gameData) {

		this.gameData = gameData;
		this.player = player;
		this.currentPlayerID = player.getPlayerID();
		dice = new Dice(); //To be used in attack phase.
	}

	public void startTurn(){
		startReinforcement();
		//startAttack();      For build 2.
		fortify();
	}

	public void clearConsole(){
		for (int line = 0; line < 50; ++line)
			System.out.println();
	}

	public void highlightStatementInRed(String str){
		System.out.print(ANSI_RED + str + ANSI_RESET);
	}

	public void boldStatement(String str){
		System.out.print(ANSI_BOLD + str + ANSI_RESET);
	}

	/*
	 * Reinforcement Phase
	 * @see com.java.controller.gameplay.ReinforcementPhase
	 */
	@Override
	public void startReinforcement() {

		Integer totalReinforcementArmyCount = calculateReinforcementArmy();
		placeArmy(totalReinforcementArmyCount);
	}

	@Override
	public Integer calculateReinforcementArmy() {

		Integer totalReinforecementArmyCount = 0;
		Integer totalCountriesOwnedByPlayer;
		

		/*Count the total number of continents owned by the player and retrieve the continent's control value. */
		HashSet<String> conqueredContinentsPerPlayer= this.gameData.gameMap.getConqueredContinentsPerPlayer(currentPlayerID);

		for(String continent: conqueredContinentsPerPlayer){
			Integer controlValue = this.gameData.gameMap.getContinent(continent).getContinentControlValue();
			totalReinforecementArmyCount += controlValue;
		}

		/*Count the total number of countries owned by the player and provide a minimum of three armies. */
		totalCountriesOwnedByPlayer= this.gameData.gameMap.getConqueredCountriesPerPlayer(currentPlayerID).size();
		totalReinforecementArmyCount += totalCountriesOwnedByPlayer/REINFORCEMENT_DIVISION_FACTOR > MINIMUM_REINFORCEMENT_ARMY_NUMBER ?
				totalCountriesOwnedByPlayer/REINFORCEMENT_DIVISION_FACTOR:MINIMUM_REINFORCEMENT_ARMY_NUMBER;

		return totalReinforecementArmyCount;
	}

	@Override
	public void placeArmy(Integer reinforcementArmy) {

		Scanner input = new Scanner(System.in);
		Integer currentPlayerID = player.getPlayerID();
		HashSet<String> countriesOwned = this.gameData.gameMap.getConqueredCountriesPerPlayer(currentPlayerID);

		this.clearConsole();
		highlightStatementInRed("Reinforcement Phase Begins...\n");
		try {
			sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.clearConsole();

		while (reinforcementArmy > 0){
			System.out.print("Total Reinforcement Army Count Remaining -> ");
			this.boldStatement("["+String.valueOf(reinforcementArmy)+"]\n");

			/*Information about the countries owned by the player and enemy countries. */
			for(String countries: countriesOwned) {
				System.out.println("\nCountry owned by you: "+countries + " ,Army Count: " + this.gameData.gameMap.getCountry(countries).getCountryArmyCount());
				HashSet<String> adjCountries = this.gameData.gameMap.getAdjacentCountries(countries);
				if(adjCountries.isEmpty()){
					System.out.println("No neighboring enemy country for country "+countries);
				}
				for(String enemyCountries: adjCountries) {
					if(this.gameData.gameMap.getCountry(enemyCountries).getCountryConquerorID() != currentPlayerID) {
						System.out.println("Neighboring Enemy country name: " +enemyCountries+ " ,Army Count: " + this.gameData.gameMap.getCountry(enemyCountries).getCountryArmyCount());
					}
				}
			}
			System.out.println("\nEnter the country name to place armies: ");
			String countryNameByUser = input.nextLine();
			/*Check for an invalid country name.*/
			if(this.gameData.gameMap.getCountry(countryNameByUser) == null) {
				this.clearConsole();
				this.highlightStatementInRed("'"+countryNameByUser+"' is an invalid country name. Please verify the country name from the list.\n\n");
				continue;
			}
			/*Check for a valid country name, but the country belonging to a different player.*/
			if(this.gameData.gameMap.getCountry(countryNameByUser).getCountryConquerorID() != currentPlayerID){
				this.clearConsole();
				this.highlightStatementInRed("'"+countryNameByUser + "' does not belong to you yet!!. Please verify your countries owned from the list below.\n\n");
				continue;
			}

			/*Information about armies and placement of armies*/
			System.out.println("Enter the number of armies to be placed, Remaining Army ("+reinforcementArmy+") :");
			try {
				Integer numberOfArmiesToBePlacedByUser = Integer.parseInt(input.nextLine());
				if (numberOfArmiesToBePlacedByUser > reinforcementArmy) {
					this.clearConsole();
					this.highlightStatementInRed("Input value '"+numberOfArmiesToBePlacedByUser+ "' should not be greater than the Total Reinforcement Army Count ");
					this.boldStatement("("+String.valueOf(reinforcementArmy)+")\n\n");
					continue;
				}
				if(!(numberOfArmiesToBePlacedByUser > 0)){
					this.clearConsole();
					this.highlightStatementInRed("Please input a value greater than 0.\n\n");
					continue;
				}
				this.clearConsole();
				this.highlightStatementInRed("Successful...Country chosen " + countryNameByUser + " ,Number of armies placed: " + numberOfArmiesToBePlacedByUser + "\n\n");
				this.gameData.gameMap.getCountry(countryNameByUser).addArmy(numberOfArmiesToBePlacedByUser);
				reinforcementArmy -= numberOfArmiesToBePlacedByUser;
			}catch (NumberFormatException ex){
				this.clearConsole();
				this.highlightStatementInRed(ex.getMessage()+", please enter numeric values only!\n\n");
				continue;
			}
		}
		input.close();
		/*End of reinforcement phase, Print the final overview.*/
		this.clearConsole();
		this.highlightStatementInRed("Reinforcement Phase is now complete. Here's an overview: \n\n");
		for(String countries: countriesOwned) {
			System.out.println("Country owned by you: "+countries + " ,Army Count: " + this.gameData.gameMap.getCountry(countries).getCountryArmyCount());
		}
	}

	/*
	 * Fortification Phase
	 * @see com.java.controller.gameplay.FortificationPhase
	 */
	@Override
	public void fortify() {
		
		// First get confirmation from the player that fortification is desired.
		// If it isn't, return and avoid the overhead of additional computation and checks.
		boolean doFortify = false;
		Scanner input = new Scanner(System.in);  
		System.out.println("Would you like to fortify? (YES/NO)");
		String playerDecision = scanner.nextLine();
		
		switch(playerDecision.toLowerCase()){
        case "yes":
        	doFortify = true;
            break;
        }
		
		if(!doFortify) {
			System.out.println("Player does not wish to fortify. Ending turn..");
			return;
		} else {
			System.out.println("Fetching potential fortification scenarios for player..");
		}
		
		// Now fetch all possibilities for player (this could get long as the game progresses and more land is acquired)
		// All logic and complexity are delegated to the getPotentialFortificationScenarios method
		HashMap<String, ArrayList<String>> fortificationScenarios = getPotentialFortificationScenarios();
		
		if(fortificationScenarios.isEmpty()) {
			System.out.println("There are currently no fortification opportunities for current player.. Sorry!");
			return;
		} 
		
		// This structure will accelerate and organize the army count process/validation
		HashMap<String, Integer> armiesPerPotentialFortificationSourceCountry = new HashMap<String, Integer>();
		
		// Print all the options out for the player to see and choose from 
		for (String keySourceCountry: fortificationScenarios.keySet()){
			armiesPerPotentialFortificationSourceCountry.put(keySourceCountry , this.gameData.gameMap.getCountry(keySourceCountry).getCountryArmyCount());
			// the range is one less because of the minimum requirement of having at least 1 army on the ground at all times.
			int possibleNumOfArmyRange = armiesPerPotentialFortificationSourceCountry.get(keySourceCountry) - 1;
            for(String correspondingDestinationCountry : fortificationScenarios.get(keySourceCountry)) {
            	System.out.println("\n"  + keySourceCountry + "\t -> \t" + correspondingDestinationCountry + "\t (up to " + possibleNumOfArmyRange + " armies)");
            }
		}
		
		// Recycle variable
		// clear the decision variable holder between choices
		playerDecision="";
			
		// while selection doesn't match any of the offered options, prompt user 
		while (! fortificationScenarios.contains(playerDecision)) {
			System.out.println("Please choose one of the suggested countries to move armies FROM: ");
			playerDecision = scanner.nextLine().toLowerCase();
		}
		String fromCountry = playerDecision;
		
		// while number of armies to be moved is not coherent, prompt user 
		// 0 is a valid selection 
		int noOfArmiesToMove = -1;
		while (! (0 <= noOfArmiesToMove < armiesPerPotentialFortificationSourceCountry.get(fromCountry))) {
			System.out.println("How many armies would you like to move from " + fromCountry + " ?");
			noOfArmiesToMove = scanner.nextInt();
		}
		
		playerDecision="";
		
		// check that the {from - to} combination specifically makes sense as a valid path
		while (! fortificationScenarios.get(fromCountry).contains(playerDecision)) {
			System.out.println("Please choose one of the valid countries to move armies INTO (knowing that you've chosen to move them from country " + fromCountry + "): ");
			playerDecision = scanner.nextLine().toLowerCase();
		}
		String toCountry = playerDecision;
		
		input.close();
		
		// At this stage all that's left to do really is adjust the army counts in the respective countries to reflect they player's fortification move
		this.gameData.gameMap.getCountry(fromCountry).deductArmy(noOfArmiesToMove);
		this.gameData.gameMap.getCountry(toCountry).addArmy(noOfArmiesToMove);
		
		System.out.println("New army count for " + fromCountry + " " + this.gameData.gameMap.getCountry(fromCountry).getCountryArmyCount());
		System.out.println("New army count for " + toCountry + " " + this.gameData.gameMap.getCountry(toCountry).getCountryArmyCount());
		
	}

	@Override
	public HashMap<String, ArrayList<String>> getPotentialFortificationScenarios() {

		// What will be returned if there are paths which can be leveraged
		HashMap<String, ArrayList<String>> fortificationScenarios  = new HashMap<String, ArrayList<String>>();
		// Helper structures for faster computation
		HashSet<String> poolOfPotentialCountries = new HashSet<String>();
		HashSet<String> adjacentCountries = new HashSet<String>();
		
		// Step 1: get the comprehensive list of all countries currently conquered by player
		poolOfPotentialCountries = this.gameData.gameMap.getConqueredCountriesPerPlayer(currentPlayerID);
		
		// Step 2: limit the scope by eliminating some of the countries as options to fortify *from*.
		// This is enforced by the known minimum requirement of at least 1 army on the ground at all times.
    	// Given that the "from" and "to" matter => key the hashmap of scenarios on "froms" and append all potential "to's" as lists of values for a "from key"
		
		for (String potentialCountry : poolOfPotentialCountries){
		    if(this.gameData.gameMap.getCountry(potentialCountry).getCountryArmyCount() > 1 ) {
		    	// once we ensure a country has more than 1 army, it becomes a potential key
		    	// ****
		    	// Step 3: now try to build the paths by delegating to the buildFortificationPath method 
		    	buildFortificationPath(fortificationScenarios , potentialCountry);
		    }
		}
		
		if(fortificationScenarios.isEmpty()) {
			return null;
		} 
		
		return fortificationScenarios;
	}
	
	@Override
	public void buildFortificationPath(HashMap<String, ArrayList<String>> fortificationScenarios, String rootCountry) {
	
		HashSet<String> adjacentCountries = new HashSet<String>();
		adjacentCountries = getAdjacentCountries(rootCountry);
		for (String adjacentCountry : adjacentCountries){
    		// need to ensure the adjacent country is also owned by that very same player - otherwise there's no path
    		if(this.gameData.gameMap.getCountry(adjacentCountry).getCountryConquerorID() == currentPlayerID) {
    			fortificationScenarios.putIfAbsent(rootCountry, new ArrayList<String>());
    			fortificationScenarios.get(rootCountry).add(adjacentCountry);
    			buildFortificationPath(adjacentCountry);
    		}
		
	}
	

}
