import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

import java.awt.*;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * Created by Alek on 6/26/2016.
 */
@ScriptManifest(name = "Cow Killer", author = "Adam", version = 42.069, info = "Cow Killer, Lumbridge", logo = "https://pbs.twimg.com/profile_images/799626330618941440/H043J55A_400x400.jpg")

public class MacroKiller extends Script {

    private Area combatArea;
    private String state = "State: Idle";
    private Font titleFont = new Font("Sans-Serif", Font.BOLD, 10);
    
    private final int hillGiantID = 2102;
    
    private long timeBegan;
    private long timeRan;
     
    private int currentLevelATT; 
    private double percentTNLATT;
    private double beginningXpATT;
    private double currentXpATT;
    private double xpGainedATT;

    private int currentLevelHP; 
    private double percentTNLHP;
    private double beginningXpHP;
    private double currentXpHP;
    private double xpGainedHP;
    
    private int currentLevelSTR; 
    private double percentTNLSTR;
    private double beginningXpSTR;
    private double currentXpSTR;
    private double xpGainedSTR;

    private int currentLevelDEF; 
    private double percentTNLDEF;
    private double beginningXpDEF;
    private double currentXpDEF;
    private double xpGainedDEF;
    
    private int currentLevelPRY; 
    private double percentTNLPRY;
    private double beginningXpPRY;
    private double currentXpPRY;
    private double xpGainedPRY;
    
    private int rng;
    private BigInteger waitTimer;
    private boolean wait = true;
    
	final int[] XP_TABLE = 
	{ 
			  0, 0, 83, 174, 276, 388, 512, 650, 801, 969, 1154,
	          1358, 1584, 1833, 2107, 2411, 2746, 3115, 3523, 3973, 4470, 5018,
	          5624, 6291, 7028, 7842, 8740, 9730, 10824, 12031, 13363, 14833,
	          16456, 18247, 20224, 22406, 24815, 27473, 30408, 33648, 37224,
	          41171, 45529, 50339, 55649, 61512, 67983, 75127, 83014, 91721,
	          101333, 111945, 123660, 136594, 150872, 166636, 184040, 203254,
	          224466, 247886, 273742, 302288, 333804, 368599, 407015, 449428,
	          496254, 547953, 605032, 668051, 737627, 814445, 899257, 992895,
	          1096278, 1210421, 1336443, 1475581, 1629200, 1798808, 1986068,
	          2192818, 2421087, 2673114, 2951373, 3258594, 3597792, 3972294,
	          4385776, 4842295, 5346332, 5902831, 6517253, 7195629, 7944614,
	          8771558, 9684577, 10692629, 11805606, 13034431, 200000000 
	};

    private Predicate<NPC> suitableNPC = n ->
            getMap().canReach(n) &&
            n.getHealthPercent() > 0 &&
            n.hasAction("Attack") &&
            combatArea.contains(n) &&
            !n.isUnderAttack() &&
            getMap().realDistance(n) < 7;


    @Override
    public void onPaint(Graphics2D g) {
    	
    	timeRan = System.currentTimeMillis() - this.timeBegan; 
    	    	
        g.setFont(titleFont);
        g.setColor(Color.WHITE);
        g.drawRect(mouse.getPosition().x - 3, mouse.getPosition().y - 3, 6, 6);
        //g.drawRect(10, 40, 10, 10);
        g.drawString("Cow Killer by Adam", 15, 50);
        g.drawString(state, 15, 70);
        g.drawString("0 Days " + String.valueOf(TimeUnit.MILLISECONDS.toHours(timeRan)) + " Hours "  + String.valueOf(TimeUnit.MILLISECONDS.toMinutes(timeRan)%60) + " Mins " + String.valueOf(TimeUnit.MILLISECONDS.toSeconds(timeRan)%60) + " Sec ", 15, 30);
        
        if(xpGainedHP > 0) {
	        g.drawString("Hitpoints: " +  String.valueOf(skills.getStatic(Skill.HITPOINTS)) + " & " + String.valueOf(Math.round(percentTNLHP)) + " % ", 15, 100);
	        g.drawString("XP Gained: "   + String.valueOf(Math.round(xpGainedHP)), 15, 120);  
	        g.drawString("XP Remaining: "   + String.valueOf(Math.round(XP_TABLE[currentLevelHP + 1]-currentXpHP)), 15, 140);  
	        g.drawString(String.valueOf(Math.round((xpGainedHP*3600000)/timeRan)) + " per hour ", 15, 160);     
	        g.drawString("Next Level in " + String.valueOf(Math.round((XP_TABLE[currentLevelHP + 1]-currentXpHP)/(xpGainedHP/(timeRan/60000)))) + " Mins ", 15, 180); 
        }
        if(xpGainedATT > 0) {
	        g.drawString("Attack: " +  String.valueOf(skills.getStatic(Skill.ATTACK)) + " & " + String.valueOf(Math.round(percentTNLATT)) + " % ", 15, 200);
	        g.drawString("XP Gained: "   + String.valueOf(Math.round(xpGainedATT)), 15, 220);  
	        g.drawString("XP Remaining: "   + String.valueOf(Math.round(XP_TABLE[currentLevelATT + 1]-currentXpATT)), 15, 240);  
	        g.drawString(String.valueOf(Math.round((xpGainedATT*3600000)/timeRan)) + " per hour ", 15, 260);     
	        g.drawString("Next Level in " + String.valueOf(Math.round((XP_TABLE[currentLevelATT + 1]-currentXpATT)/(xpGainedATT/(timeRan/60000)))) + " Mins ", 15, 280); 
        }
        if(xpGainedSTR > 0) {
	        g.drawString("Strength: " +  String.valueOf(skills.getStatic(Skill.STRENGTH)) + " & " + String.valueOf(Math.round(percentTNLSTR)) + " % ", 15, 320);
	        g.drawString("XP Gained: "   + String.valueOf(Math.round(xpGainedSTR)), 15, 340);
	        g.drawString("XP Remaining: "   + String.valueOf(Math.round(XP_TABLE[currentLevelSTR + 1]-currentXpSTR)), 15, 360);  
	        g.drawString(String.valueOf(String.valueOf(Math.round((xpGainedSTR*3600000)/timeRan))) + " per hour ", 15, 380);  
	        g.drawString("Next Level in " + String.valueOf(Math.round((XP_TABLE[currentLevelSTR + 1]-currentXpSTR)/(xpGainedSTR/(timeRan/60000)))) + " mins ", 15, 400); 
        }
        if(xpGainedDEF > 0) {
	        g.drawString("Defence: " +  String.valueOf(skills.getStatic(Skill.DEFENCE)) + " & " + String.valueOf(Math.round(percentTNLDEF)) + " % ", 15, 440);
	        g.drawString("XP Gained: "   + String.valueOf(Math.round(xpGainedDEF)), 15, 460);
	        g.drawString("XP Remaining: "   + String.valueOf(Math.round(XP_TABLE[currentLevelDEF + 1]-currentXpDEF)), 15, 480);  
	        g.drawString(String.valueOf(String.valueOf(Math.round((xpGainedDEF*3600000)/timeRan))) + " per hour ", 15, 500);  
	        g.drawString("Next Level in " + String.valueOf(Math.round((XP_TABLE[currentLevelDEF + 1]-currentXpDEF)/(xpGainedDEF/(timeRan/60000)))) + " Mins ", 15, 520); 
        }
        if(xpGainedPRY > 0) {
	        g.drawString("Prayer: " +  String.valueOf(skills.getStatic(Skill.PRAYER)) + " & " + String.valueOf(Math.round(percentTNLPRY)) + " % ", 15, 560);
	        g.drawString("XP Gained: "   + String.valueOf(Math.round(xpGainedPRY)), 15, 580);
	        g.drawString("XP Remaining: "   + String.valueOf(Math.round(XP_TABLE[currentLevelPRY + 1]-currentXpPRY)), 15, 600);  
	        g.drawString(String.valueOf(String.valueOf(Math.round((xpGainedPRY*3600000)/timeRan))) + " per hour ", 15, 620);  
	        g.drawString("Next Level in " + String.valueOf(Math.round((XP_TABLE[currentLevelPRY + 1]-currentXpPRY)/(xpGainedPRY/(timeRan/60000)))) + " Mins ", 15, 640); 
        }
        
        g.drawString("Wait Timer: " + String.valueOf(waitTimer.subtract(BigInteger.valueOf(System.currentTimeMillis()))) + " ms ", 15, 680); 
        g.drawString("Wait Boolean: " + wait , 15, 700); 
        g.drawString("WaitTimer: " + waitTimer , 15, 720); 
        
    }
    

    @Override
    public void onStart() {

        combatArea = myPlayer().getArea(6);
        beginningXpATT = skills.getExperience(Skill.ATTACK); 
        beginningXpSTR = skills.getExperience(Skill.STRENGTH); 
        beginningXpDEF = skills.getExperience(Skill.DEFENCE); 
        beginningXpHP = skills.getExperience(Skill.HITPOINTS); 
        beginningXpPRY = skills.getExperience(Skill.PRAYER); 
         
        timeBegan = System.currentTimeMillis();
    }

    Filter<NPC> monsterFilter = new Filter<NPC>() {
        @Override
        public boolean match(NPC n) {
			if(n.getId() != hillGiantID) return false; 		//hill giant ID
			if(n.isUnderAttack()) return false;				//not under attack 
			if(n.isAnimating()) return false;
            return true;
        }
    };
    
    @Override
    public int onLoop() throws InterruptedException {
    	//calculate gained xp rates
    	currentXpATT = skills.getExperience(Skill.ATTACK);
    	xpGainedATT = currentXpATT - beginningXpATT; 
    	currentLevelATT = skills.getStatic(Skill.ATTACK);
    	percentTNLATT = ((currentXpATT - XP_TABLE[currentLevelATT]) / (XP_TABLE[currentLevelATT + 1] - XP_TABLE[currentLevelATT])) * 100;  
    	
    	currentXpSTR = skills.getExperience(Skill.STRENGTH);
    	xpGainedSTR = currentXpSTR - beginningXpSTR; 
    	currentLevelSTR = skills.getStatic(Skill.STRENGTH);
    	percentTNLSTR = ((currentXpSTR - XP_TABLE[currentLevelSTR]) / (XP_TABLE[currentLevelSTR + 1] - XP_TABLE[currentLevelSTR])) * 100;
    	
    	currentXpDEF = skills.getExperience(Skill.DEFENCE);
    	xpGainedDEF = currentXpDEF - beginningXpDEF; 
    	currentLevelDEF = skills.getStatic(Skill.DEFENCE);
    	percentTNLDEF = ((currentXpDEF - XP_TABLE[currentLevelDEF]) / (XP_TABLE[currentLevelDEF + 1] - XP_TABLE[currentLevelDEF])) * 100;
    	
    	currentXpHP = skills.getExperience(Skill.HITPOINTS);
    	xpGainedHP = currentXpHP - beginningXpHP; 
    	currentLevelHP = skills.getStatic(Skill.HITPOINTS);
    	percentTNLHP = ((currentXpHP - XP_TABLE[currentLevelHP]) / (XP_TABLE[currentLevelHP + 1] - XP_TABLE[currentLevelHP])) * 100;

    	currentXpPRY = skills.getExperience(Skill.PRAYER);
    	xpGainedPRY = currentXpPRY - beginningXpPRY; 
    	currentLevelPRY = skills.getStatic(Skill.PRAYER);
    	percentTNLPRY = ((currentXpPRY - XP_TABLE[currentLevelPRY]) / (XP_TABLE[currentLevelPRY + 1] - XP_TABLE[currentLevelPRY])) * 100;
    	
    	
    	//random anti-pattern events
    	rng = random(0,250);
    			
    	if(rng == 0) {
    		//1 in 250 chance
    		mouse.moveOutsideScreen();
    	} else if(rng == 1) {
    		//1 in 250 chance
    		mouse.scrollUp();
    	} else if(rng == 2) {
    		//1 in 250 chance
    		mouse.scrollDown();
    	} else if(rng == 3) {
    		//1 in 250 chance
    		wait(random(5000,10000));
    	} else if(rng == 4) {
    		//1 in 250 chance
    		wait(random(5000,10000));
    		
    	}  else if(rng == 5 && ( currentLevelATT < currentLevelSTR-5/* || currentLevelATT < currentLevelDEF-5*/) ) {
    		//Change to Attack
    		getTabs().open(Tab.ATTACK);
    		 
    		if(getConfigs().get(43) != 0) { 
    			if (getWidgets().get(593, 6).interact()) {
    				new ConditionalSleep(3000) {
    					@Override
    					public boolean condition() throws InterruptedException {
    						return getConfigs().get(43) == 0;
    					}
    				}.sleep();
    			}
    		}
    	}else if(rng == 6 && ( currentLevelSTR < currentLevelATT-5/* || currentLevelSTR < currentLevelDEF-2*/)) {
    		//Change to Strength
    		getTabs().open(Tab.ATTACK);
    		 
    		if(getConfigs().get(43) != 1) { 
    			if (getWidgets().get(593, 10).interact()) {
    				new ConditionalSleep(3000) {
    					@Override
    					public boolean condition() throws InterruptedException {
    						return getConfigs().get(43) == 0;
    					}
    				}.sleep();
    			}
    		}
    	}/* else if(rng == 7 && ( currentLevelDEF < currentLevelATT-2 || currentLevelDEF < currentLevelSTR-2) ) {
    		//Change to Defence
    		getTabs().open(Tab.ATTACK);
    		 
    		if(getConfigs().get(43) != 11) { 
    			if (getWidgets().get(593, 18).interact()) {
    				new ConditionalSleep(3000) {
    					@Override
    					public boolean condition() throws InterruptedException {
    						return getConfigs().get(43) == 0;
    					}
    				}.sleep();
    			}
    		}
    	}*/else if(rng == 8) {
    		getTabs().open(Tab.ATTACK);
    		
    	}else if(rng == 9 ) {
    		getTabs().open(Tab.SKILLS);
    		
    	}else if(rng == 10 && !getCombat().isFighting() && !myPlayer().isUnderAttack() && !myPlayer().isAnimating() && !myPlayer().isMoving()) {
        	//lets go for a walk
        	//getWalking().webWalk(new Position(random(3255,3264),random(3257,3294),0) );
        	
    	} else if(rng > 200) {
    		//4 in 5 chance
    		getTabs().open(Tab.INVENTORY);
    		 
    	}
		 

    	
        if (getSkills().getDynamic(Skill.HITPOINTS) < (getSkills().getStatic(Skill.HITPOINTS) / 2)) {
            state = "State: Looking for food to eat";
            Optional<Item> foodItem = Arrays.stream(getInventory().getItems()).filter(i -> i != null && (i.hasAction("Eat") || i.hasAction("Drink"))).findFirst();
            if (foodItem.isPresent()) {
                state = "State: Eating food " + foodItem.get().getName();
                foodItem.get().interact("Eat", "Drink");
            } else {
                state = "State: No food remaining, logging out";
                stop(true);
            }
        } else {
        
	        if(myPlayer().isUnderAttack() || myPlayer().isAnimating() || myPlayer().isMoving() || getCombat().isFighting()) {
	        	//Player is under attack, keep an eye on health/logout if no food remains
	
	        	//finds food to eat, if none remains logout to prevent death
	        	if (getSkills().getDynamic(Skill.HITPOINTS) < (getSkills().getStatic(Skill.HITPOINTS) / 2)) {
	                state = "State: Looking for food to eat";
	                Optional<Item> foodItem = Arrays.stream(getInventory().getItems()).filter(i -> i != null && (i.hasAction("Eat"))).findFirst();
	                if (foodItem.isPresent()) {
	                    state = "State: Eating food " + foodItem.get().getName();
	                    foodItem.get().interact("Eat", "Drink");
	                } else {
	                    state = "State: No food remaining, logging out";
	                    stop(true);
	                }
	            } 
	        	
	        	
	        } else {
	        	//player is not under attack, find next target
	        	state = "State: Searching for monsters to kill";
	        	Area searchableArea = new Area(new Position(3098, 9828, 0), new Position(3120, 9821, 0));

	        	state = "State: waiting to find npc";
	        	
	        	if(wait) {
	        		

	            	
		            List<NPC> npcs = getNpcs().filter(monsterFilter);
		            
		            
		            if (!npcs.isEmpty()) {
		            	//npc is in attack range
		                npcs.sort(Comparator.<NPC>comparingInt(a -> getMap().realDistance(a)).thenComparingInt(b -> getMap().realDistance(b)));
		                if (npcs.get(0).interact("Attack")) {
		                    state = "State: Attacking " + npcs.get(0).getName();

		                }
		            } else {
		            	//can't find target
		            }
	        	}else {
	        		state= "State: waiting to attack npc.";
	        	}
	        }
        }
        return 500;
    }
    


}