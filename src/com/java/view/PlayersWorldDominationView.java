package com.java.view;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.java.model.Observable;
import com.java.model.map.GameMap;
import com.java.model.player.Player;

public class PlayersWorldDominationView implements GameView {
	
	private File playersWorldDominationFile;
    private FileWriter editView;
    
    public PlayersWorldDominationView() {
    	
    	playersWorldDominationFile = new File("./PlayersWorldDominationView.txt");

        if(playersWorldDominationFile.exists()){
            playersWorldDominationFile.delete();
        }
        
        setUpFile();
    }
    
    private void setUpFile() {
    	try{
            playersWorldDominationFile.createNewFile();
            editView = new FileWriter(playersWorldDominationFile);
            editView.write(""); // to clear the view
            editView.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    	
    }
    
	@Override
	public void update(Observable observable) {
		
		if(observable instanceof GameMap) {
			try {
            	setUpFile();
            	
            	HashMap<String,Double> ownershipPercentage = ((GameMap) observable).getOwnershipPercentage();
            	HashMap<Integer, HashSet<String>> conqueredContinentsPerPlayer = ((GameMap) observable).getConqueredContinentsPerPlayer();
            	HashMap<String,Integer> numberOfArmiesPerPlayer = ((GameMap) observable).getNumberOfArmiesPerPlayer();
            	ArrayList<Player> playersInfo = ((GameMap) observable).getPlayersInfo();
            	HashSet<String> continentsPerPlayer;
            	
                editView.write("\nPercentage of the map controlled by every player:\n");
                
                for(Entry<String, Double> entry : ownershipPercentage.entrySet()) {
                	
                	editView.write("\nPlayer: " + entry.getKey() + "\t\t\t" + String.format("%.2f", entry.getValue()) + "%");
                	
                }
                
                editView.write("\n\nContinents controlled by every player:\n");
                
                for(Player player : playersInfo) {
                	
                	editView.write("\nPlayer: " + player.getPlayerID() + " " + player.getPlayerName());
                	continentsPerPlayer = conqueredContinentsPerPlayer.get(player.getPlayerID());
                	
                	if(continentsPerPlayer == null) {
                		editView.write("\n\t\t");
                	} else {
                		for(String continent : continentsPerPlayer) {
                    		editView.write("\n\t\t\t" + continent);
                    	}
                	}
                	editView.write("\n");
                }
                editView.write("\n");
                editView.write("\nNumber of armies per player:\n");
                
                if(numberOfArmiesPerPlayer != null ) {
                	for(Player player : playersInfo) {
                    	editView.write("\nPlayer " + player.getPlayerID() + " " + player.getPlayerName()+ " has " + numberOfArmiesPerPlayer.get(player.getPlayerID().toString()) + " armies.");
                    }
                }
                
                editView.close();
                
			} catch (IOException e) {
                e.printStackTrace();
            }
		}
		
	}

}