package com.java.model.player;

import java.io.Serializable;

public class Player implements Serializable {
    private PlayerStrategy strategyType;

    public void setStrategyType(PlayerStrategy strategyType){
        this.strategyType = strategyType;
    }


    public PlayerStrategy getStrategyType() {
        return strategyType;
    }

    // start turn to initiate the Strategy type.
    /**
     * The startTurn() method organizes the flow of the game by ordering
     * phase-execution.
     */
    public void startTurn() {
        boolean isWinner = false;
        startReinforcement();
        startAttack();
        // attack phase changes state so before going to fortify logic, check
         isWinner = checkIfPlayerHasConqueredTheWorld();
        if (!isWinner) {
            startFortification();
        }
    }

    public void startReinforcement() {
       strategyType.executeReinforcement();
    }
    public void startAttack() {
        strategyType.executeAttack();
    }

    public void startFortification() {
        strategyType.executeFortification();
    }

    public boolean checkIfPlayerHasConqueredTheWorld(){
        return strategyType.checkIfPlayerHasConqueredTheWorld();
    }
}
