package com.java.model.player;

import com.java.model.cards.Card;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AggresiveMode extends PlayerStrategy {

    public AggresiveMode(Integer playerID, String playerName) {

        super(playerID,playerName);

    }

    @Override
    public void executeReinforcement() {

    }

    @Override
    public ArrayList<Card> getValidCards() {
        return null;
    }

    @Override
    public void placeArmy(Integer reinforcementArmy) {

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
