import com.java.controller.gameplay.ReinforcementTest;
import com.java.controller.gameplay.FortificationTest;
import com.java.controller.gameplay.AttackTest;

import com.java.controller.map.MapTest;
import com.java.controller.startup.StartUpPhaseTest;
import com.java.model.map.GameMapTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)

@SuiteClasses({ReinforcementTest.class, FortificationTest.class, AttackTest.class, StartUpPhaseTest.class, MapTest.class, GameMapTest.class})

/**
 * This class is the suit to run the test cases of every test class.
 * 
 * @author Arnav Bhardwaj
 * @author Karan Dhingra
 * @author Ghalia Elkerdi
 * @author Sahil Singh Sodhi
 * @author Cristian Rodriguez 
 * @version 1.0.0
 * */
public class junit4TestSuite {
}
