
package com.java.controller.startup;

import com.java.controller.dice.Dice;
import com.java.model.cards.CardsDeck;
import com.java.model.gamedata.GameData;
import com.java.model.map.Country;
import com.java.model.map.GameMap;
import com.java.model.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;


/**
 * StartupPhase includes the logic to set up the game before it starts.
 * it will arrange the players in a round robin fashion, based on the highest number generated by dice.
 * Each country will be assigned randomly to the players in play.
 * The initial army calculation is based on the number of players.
 * provided a sort method to resolve ties between players in order to establish the round robin order.
 * 
 * @author Arnav Bhardwaj
 * @author Karan Dhingra
 * @author Ghalia Elkerdi
 * @author Sahil Singh Sodhi
 * @author Cristian Rodriguez 
 * @version 1.0.0
 */
public class StartUpPhase {
	
	/**
	 * Game Data holds the whole data of the game, here the static variable holds the ids for the players.
	 * In that way it is possible to identify players even if the have the same name.
	 * @see GameData
	 */
	public GameData gameData;
	private static int playerId = 1;
	
	/**
	 * Default constructor receives game data as a parameter. Because this is the set up phase class every data
	 * is going to be calculated and set.
	 * @param gameData the game data object be set up.
	 */
	public StartUpPhase(GameData gameData) {
		this.gameData = gameData;
	}
	
	/**
	 * It assigns unique ids to the players by receiving a collection of player names.
	 * the static variable is incremented by one every time a new player is created.
	 * @param playerNames list of names passed based on the console input from user
	 * @return A list that contains all the players which now have ids assigned.
	 */
	public ArrayList<Player> generatePlayers(ArrayList<String> playerNames){
		
		// A collection of empty players is created.
		ArrayList<Player> newPlayers = new ArrayList<Player>();
		
		// Give every new player a unique id and the name provided by the user.
		for(int i=0; i < playerNames.size(); i++){
			newPlayers.add(new Player(playerId,playerNames.get(i)));
			playerId++;
		}
		
		// Set the player collection with the new one.
		gameData.setPlayers(newPlayers);
		
		// Sets the player list in order of play.
		gameData.gameMap.setupPlayerNames(newPlayers);
		
		return newPlayers;
	}

	public void generateCardsDeck() {
		HashMap<String, Country> countryObject = gameData.gameMap.getAllCountries();
		ArrayList<Country> countryList = new ArrayList<Country>();
		for(Country country : countryObject.values()){
			countryList.add(country);
		}
		gameData.cardsDeck = new CardsDeck(countryList);
	}
	
	/**
	 * The approach to give the countries to the players is random. That means that each contry is randomly
	 * given to any player. But each player receives the same number of countries.
	 * 
	 */
	public void assignCountriesToPlayers() {
		
		// Get the list of players from game data.
		ArrayList<Player> players = gameData.getPlayers();

		// Used to obtain the country objects
		HashMap<String, Country> countryObject = gameData.gameMap.getAllCountries();

		// Start randomly assigning the countries evenly to the players
		String[] countiresToAssign = Arrays.copyOf(countryObject.keySet().toArray(), countryObject.size(),String[].class);

		// Converting to arraylist for shuffling the elements (countries)
		ArrayList<String> countiresToAssignArrayList =  new ArrayList<String>(Arrays.asList(countiresToAssign));
		
		// The order of the countries are randomly shuffle in order to give them one by one.
		Collections.shuffle(countiresToAssignArrayList);
		
		// Helper variables to assign the countries.
		int numOfCountriesToAssign = countiresToAssignArrayList.size();
		int numberOfPlayers = gameData.getNoOfPlayers();
		
		// This index keeps track of the current index of array countries
		int indexKeeper = 0;
		
		// Iterating the players to assign them the countries in a fair way.
		for(int i = 0; i < numberOfPlayers; i++) {
			int numberOfCountriesToBeAssignedToPlayer = (numOfCountriesToAssign - indexKeeper) / (numberOfPlayers - i);
			for(int j = indexKeeper; j < indexKeeper + numberOfCountriesToBeAssignedToPlayer; j++) {
				gameData.gameMap.setCountryConquerer(countiresToAssignArrayList.get(j), players.get(i).getPlayerID());
			}
			indexKeeper += numberOfCountriesToBeAssignedToPlayer;
		}
				
		// Calculates the % ownership
		gameData.gameMap.calculateOwnershipPercentage();
		
		// Calculates continent Ownership
		//gameData.gameMap.calculateContinentOwnership();
	}
	
	/**
	 * The initial army calculation is given by the number of players.
	 * @param numPlayers based on the number of players the method returns the initial army per player.
	 * @return the value that provides total initial army ammount per player
	 */
	public Integer initialArmyCalculation(Integer numPlayers) {
		Integer numberOfArmiesPerPlayer = new Integer(0);
		
		Double factor = ((double)gameData.gameMap.getNumberOfCountries() / (double)gameData.gameMap.DEFAULT_NUMBER_OF_COUNTRIES);
		
		if(factor < 1) {
			factor = 1.0;
		}
		
		switch(numPlayers) {
			case 2:
				numberOfArmiesPerPlayer = 40;
				break;
			case 3:
				numberOfArmiesPerPlayer = 35;
				break;
			case 4:
				numberOfArmiesPerPlayer = 30;
				break;
			case 5:
				numberOfArmiesPerPlayer = 25;
				break;
			case 6:
				numberOfArmiesPerPlayer = 20;
				break;
		}
		return (int) (numberOfArmiesPerPlayer * factor);
	}
	
	/**
	 * The order of play is given my the max result of the dices after every player roll a dice. If there is
	 * a tie the player roll again until one has a greter result.
	 * @return List of all players with proper order in which they will play for the rest of the game
	 */
	public ArrayList<Player> generateRoundRobin(){

		// Auxiliary variables to perform the order of player calculation.
		ArrayList<Player> temp = new ArrayList<Player>();
		ArrayList<Player> tiePlayers = gameData.getPlayers();
		Boolean flag = false;
		
		// Dice object to get the random numbers from 1 to 6
		Dice dice = new Dice();
		int aux = tiePlayers.size();

		// The following loop will resolve ties based on the die rolled.
		while(!tiePlayers.isEmpty()) {

			// Assign the player object to the roll die integer amount
			for(int k = 0; k < aux; k++){
				try {
					tiePlayers.get(k).setOrderOfPlay(dice.rollDice());
				} catch(Exception e) {
					// If something goes wrong with the return.
					for(Player tie : tiePlayers) {
						temp.add(tie);
					}
					break;
				}
				//System.out.println("Player " + tiePlayers.get(k).getPlayerName() + " rolled the dice: " + tiePlayers.get(k).getOrderOfPlay());
			}

            // When the player roll the dice the tie has to be resolved by calling custom sort from 0 till the index of tie player.
			tiePlayers = sort(tiePlayers, 0, aux);

            // To check if the first two players do not have the same number rolled add to temp array
			if(tiePlayers.size() > 1 && tiePlayers.get(0).getOrderOfPlay() != tiePlayers.get(1).getOrderOfPlay()) {
				temp.add(tiePlayers.get(0));
				tiePlayers.remove(0);
			}
			
			if(tiePlayers.size() == 1) {
				temp.add(tiePlayers.get(0));
				tiePlayers.remove(0);
			}
			
			// Gets the index of tie players.
			for(int i = 0; i < tiePlayers.size() - 1; i++) {
				try {
					if(tiePlayers.get(0).getOrderOfPlay() == tiePlayers.get(i+1).getOrderOfPlay()) {
						aux = i + 1 + 1;
						flag = true;

					} else {
						if(!flag) {
							aux = 0;
						}
						break;
					}
				} catch(Exception e) {
					// If something goes wrong with the return.
					for(Player tie : tiePlayers) {
						temp.add(tie);
					}
					break;
				}
			}
		}
		
		// Sets the order of play.
		for(int i = 0; i < temp.size(); i++) {
			temp.get(i).setOrderOfPlay(i+1);
		}
		
		// Sets the player list in order of play.
		gameData.setPlayers(temp);
		
		return gameData.getPlayers();
	}

	/**
	 * Custom sorting that allows to sort from a given index to another index of the array
	 * to be sorted.
	 * 
	 * @param array Array to be sorted.
	 * @param from index from where to start sorting.
	 * @param to index from where to end sorting.
	 * @return List of players with sorted order from given index to end
	 * 
	 */
	private ArrayList<Player> sort(ArrayList<Player> array, int from, int to) {
		
		// Each element of the array is compared with the rest of elements.
		for(int i = from; i < to; i++) {
			for(int j = from; j < to; j++) {
				try {
					if(array.get(i).getOrderOfPlay() > array.get(j).getOrderOfPlay() ) {
						Player temp = array.get(i);
						array.set(i, array.get(j));
						array.set(j, temp);
					}
				}
				catch(Exception e) {
					// If something goes wrong with the return.
					return array;
				}
			}
		}
		return array;
	}
}
