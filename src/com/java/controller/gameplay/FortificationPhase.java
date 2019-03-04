package com.java.controller.gameplay;

import java.util.ArrayList;
import java.util.HashMap;

public interface FortificationPhase {

	HashMap<String, ArrayList<String>> getPotentialFortificationScenarios();
	void buildFortificationPath(HashMap<String, ArrayList<String>> fortificationScenarios, String rootCountry);
	
}
