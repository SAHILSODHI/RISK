package com.java.model.player;

public class BenevolentPlayerStrategy extends Player{

    /**
     * Creates a player by giving the id and the name
     *
     * @param playerID   the player id.
     * @param playerName the player name.
     */
    public BenevolentPlayerStrategy(Integer playerID, String playerName) {
        super(playerID, playerName);
        reinforce = new BenevolentReinforce(playerID, playerName);
    }
}
