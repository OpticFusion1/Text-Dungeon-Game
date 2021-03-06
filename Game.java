import java.util.Scanner;
import java.io.File;
import java.util.Random;
import java.util.ArrayList;

//Text based adventure game
public class Game {

	//Static variable declaration
	final static String COMMANDS = "adrfqshi";
	/*
		Attack
		Defend
		Rest
		Flee
		Quit
		Stats
		Help
	*/

	//Program entrypoint
	public static void main(String[] args) {
		new Game();
	}

	//Game variables
	Monster player;
	Monster enemy = null;
	String randomItem;
	Scanner scanner;
	Random rand;

	String[] monsterNames;

	int experience = 0;
	int level = 1;
	int experienceToLevel = 15;

	static int textSpeed = 1;

	//Game constructor and beginning
	public Game() {

		//Initialize scanner and new random object
		scanner = new Scanner(System.in);
		rand = new Random();

		//Load splash text into blueprint builder
		BlueprintBuilder.addBlueprint("data/splash.txt","splash");

		//Load monster files
		File[] rawFiles = new File("data/monsters").listFiles();
		int fileCount = rawFiles.length;

		ArrayList<File> monsterFiles = new ArrayList<File>();

		for (File f : rawFiles) {
			if (f.getName().charAt(0) != '.') {
				monsterFiles.add(f);
			}
		}

		monsterNames = new String[monsterFiles.size()];
		//Load monsters into blueprint builder
		for (int i=0;i<monsterFiles.size();i++) {
			//Get monster name from file name
			String name = monsterFiles.get(i).getName().replace(".txt","");

			//Add monster to blueprint builder
			BlueprintBuilder.addBlueprint(monsterFiles.get(i).getPath(),name);
			monsterNames[i] =  name;
		}

		//Initialize player and monster refrence object
		player = new Monster("You",5,5,5,10,0);

		//Get random player item
		String items = BlueprintBuilder.blueprintParseString("splash","dungeonObjects");
		randomItem = randomString(items);

		//Get text speed
		System.out.println("Please select a text scroll speed");
		System.out.println("0 - Instant\n1 - Fast\n2 - Normal\n3 - Slow");
		textSpeed = getCommand("0123",scanner) - '0';

		//Begin game!!
		slowPrint("You enter the dungeon, armed with nothing but your wits, cunning, and a " + randomItem + ". What adventure awaits you in these long forgotten catacombs?");
		pause(500);
		System.out.println("Press enter");
		scanner.nextLine();

		doTurn();
	}

	//Get the player's action, and display info
	void doTurn() {

		System.out.println();
		pause(100);

		System.out.println("----------");

		//Test for enemy
		if (enemy == null || enemy.isDead()) {
			//Randomize enemy
			enemy = randomMonster(level);
			slowPrint(formatString("A new enemy, [ENEMY], attacks!"));
		}else{
			slowPrint(formatString(enemy.getSplashMessage()));
		}
		//Get the player's command
		slowPrint("You are at " + player.getHealth() + " health!",10);
		slowPrint("What will you do?",10);

		char command = getCommand(COMMANDS,scanner);

		doCombatTurn(command);

	}

	//Do the player's chosen action
	void doCombatTurn(char action) {

		System.out.println();

		//Quit the game
		if (action == 'q') {
			slowPrint("Quitting",false);
			slowPrint("... ",250);
			System.exit(0);//Quit the game
		}

		//Do something else
		boolean isEnemyAttacking = enemy.willAttack();
		boolean isPlayerDefending = false;
		boolean didCrit = false;;

		//What do we do?....
		switch (action) {

			case 'a':
				//Attack!
				slowPrint(formatString("You attack the [ENEMY]!"));
				pause(100);

				//Do we hit?
				if (player.getSpeed() + rollDice(6) < enemy.getSpeed()) {
					slowPrint(formatString("The [ENEMY] dodges the attack!"));
					break;//Exit attack block early
				}


				//Is the enemy defending?
				if (!isEnemyAttacking) {
					slowPrint(formatString("The [ENEMY] defends themself!"));
				}

				int playerDamageAmount = player.getAttack();
				System.out.println();

				//Critical hit
				if (rollDice(20) == 20) {
					slowPrint("Critical hit! Damage doubled");
					String critMessage = randomString(BlueprintBuilder.blueprintParseString("splash","critMessages"));
					slowPrint(formatString(critMessage)+"\n");
					didCrit = true;
					playerDamageAmount *= 2;
				}

				//Attack the enemy and find out how much damage is dealt
				int damageTaken = enemy.takeDamage(playerDamageAmount,!isEnemyAttacking);
				slowPrint(formatString("The [ENEMY] takes " + damageTaken + " damage!"));

				if (enemy.isDead()) {

					slowPrint(formatString(enemy.getDeathMessage()));
					slowPrint(formatString("[ENEMY] has been slain!"));

					//Gain experience
					int experienceAmount = enemy.getExperience();
					if (didCrit) {
						experienceAmount *= 2;
					}
					experience += experienceAmount;
					slowPrint("Gained " + experienceAmount + " xp! (" + experience + "/" + experienceToLevel + ")");
					
					while (experience >= experienceToLevel) {
						//Player level up!
						levelUp();
					}

					//Skip back to the next turn
					pause(200);
					doTurn();
				}

				break;

			case 'd':
				//Defend!
				String defendMessage = randomString(BlueprintBuilder.blueprintParseString("splash","defendMessages"));
				slowPrint(formatString(defendMessage));
				isPlayerDefending = true;
				break;

			case 'f':
				//Flee!

				//Calculate chance of success
				double chance = (double)player.getHealth() + player.getSpeed()/player.getMaxHealth();
				//You flee if your health percentage is greater than the enemy's agression
				if (chance >= enemy.getAgression()) {
					//Success!
					slowPrint(formatString("You flee from the [ENEMY]!"));
					enemy = null;
					doTurn();
				}else{
					//Failure!!
					String fleeMessage = randomString(BlueprintBuilder.blueprintParseString("splash","fleeFailureMessages"));
					slowPrint(formatString(fleeMessage));
				}
				break;

			case 'r':
				//Rest!
				String restMessage = randomString(BlueprintBuilder.blueprintParseString("splash","restMessages"));
				slowPrint(formatString(restMessage));
				int amount = rand.nextInt(level/2+4)+level/2;
				player.heal(amount);
				slowPrint("Recovered " + amount + " health!");
				break;


			case 's':
				//Display stats
				String statString = playerStatString();
				statString += "\n\nExperience: " + experience + "/" + experienceToLevel + " [Level " + level + "]";
				statString += "\n\nInventory: " + randomItem;
				slowPrint(statString,10);

				doTurn();
				break;

			case 'h':
				//Help
				slowPrint("[A]ttack - Deal damage to the enemy\n[D]efend - Absorb damage from the enemy\n[R]est - Regain lost health\n[F]lee - Run away\n[S]tats - View your stats\n[I]nspect Enemy - View the enemy's stats\n[Q]uit - Quit the game",10);
				slowPrint("\nStats:\nAttack - How much damage you deal\nDefense - How much damage you can absorb\nSpeed - Increases odds of dodging attacks and fleeing",10);
				doTurn();
				break;

			case 'i':
				//Inspect the enemy

				slowPrint(enemy.getName()+":",5);
				slowPrint(enemy.getDescription(),15);

				slowPrint(enemy.getHealth() + "/" + enemy.getMaxHealth() + " health",15);
				slowPrint(enemy.getAttack() + " attack",15);
				slowPrint(enemy.getDefense() + " defense",15);
				slowPrint(enemy.getSpeed() + " speed",15);

				doTurn();
				break;
		}

		//Enemy attacking!
		if (isEnemyAttacking) {
			//Attack!
			slowPrint(formatString("The [ENEMY] attacks you!!"));
			pause(100);

			//Do they hit?
			if (player.getSpeed() - rollDice(6) + (!isPlayerDefending ? 2 : 0) > enemy.getSpeed()) {
				slowPrint(formatString("You dodge the [ENEMY]'s attack!"));
				doTurn();//Exit attack block early
			}

			int enemyDamageAmount = enemy.getAttack();
			//Attack the enemy and find out how much damage is dealt
			int damageTaken = player.takeDamage(enemyDamageAmount,isPlayerDefending);
			slowPrint(formatString("You take " + damageTaken + " damage!"));

			//Did the player die?
			if (player.isDead()) {
				String deathMessage = randomString(BlueprintBuilder.blueprintParseString("splash","deathMessages"));
				slowPrint(formatString(deathMessage));
				System.out.println("Game over!");
				System.exit(0);
			}

		}else{
			slowPrint(formatString("The [ENEMY] decides not to attack you "));
		}

		//Go back to the turn
		doTurn();
	}

	//Create a random monster
	Monster randomMonster(int maxDifficulty) {

		int diff = maxDifficulty+1;
		int minDifficulty = maxDifficulty - 2;
		String internalName = "";

		while (diff > maxDifficulty || diff < minDifficulty) {
			int i = rand.nextInt(monsterNames.length);
			internalName = monsterNames[i];

			diff = BlueprintBuilder.blueprintParseInt(internalName,"difficulty");
		}
 
 		int halfLevel = level/2;
 		int modifier = 0;
 		if (halfLevel > 0) {
			modifier = rand.nextInt(halfLevel) + halfLevel;
		}	

		String name = BlueprintBuilder.blueprintParseString(internalName,"name");
		int health = BlueprintBuilder.blueprintParseInt(internalName,"health") + modifier;
		int attack = BlueprintBuilder.blueprintParseInt(internalName,"attack") + modifier;
		int defense = BlueprintBuilder.blueprintParseInt(internalName,"defense") + modifier;
		int speed = BlueprintBuilder.blueprintParseInt(internalName,"speed") + modifier;
		double agression = BlueprintBuilder.blueprintParseDouble(internalName,"agression");

		Monster m = new Monster(name,attack,defense,speed,health,agression);

		String splashMessages = BlueprintBuilder.blueprintParseString(internalName,"splashMessages");
		if (splashMessages.length() > 0) m.setSplashMessages(splashMessages);

		String deathMessage = BlueprintBuilder.blueprintParseString(internalName,"deathMessage");
		if (deathMessage.length() > 0) m.setDeathMessage(deathMessage);

		String desc = BlueprintBuilder.blueprintParseString(internalName,"description");
		if (desc.length() > 0) m.setDescription(desc);

		return m;
	}


	//Level up the player
	private void levelUp() {

		System.out.println("~~~~~~");

		level++;
		experience -= experienceToLevel;

		//Display level up info
		slowPrint("Level up! [" + level + "]",10);
		slowPrint("HP increased and fully restored!");
		slowPrint("What do you want to level up?\n[A]ttack\n[D]efense\n[S]peed");
		char action = getCommand("ads",scanner);

		//Upgrade something
		switch (action) {
			case 'a':
				player.increaseAttack();
				slowPrint("Attack increased to " + player.getAttack() + "!");
				break;

			case 'd':
				player.increaseDefense();
				slowPrint("Defense increased to " + player.getAttack() + "!");
				break;

			case 's':
				player.increaseSpeed();
				slowPrint("Speed increased to " + player.getSpeed() + "!");
				break;
		}

		player.setHealth(player.getMaxHealth()+1);

		experienceToLevel *= 2;

	}

	private String playerStatString() {
		String statString = "Health: " + player.getHealth() + "/" + player.getMaxHealth() + "\nAttack: " + player.getAttack() + "\nDefense: " + player.getDefense() + "\nSpeed: " + player.getSpeed();
		return statString;
	}



	//UTILITY COMMANDS
	//Pause for a specified amount of milliseconds
	public static void pause(int millis) {
		try {
			Thread.sleep(millis);
		}catch (java.lang.InterruptedException e) {
			e.printStackTrace();
		}
	}

	//Print a message over time
	public static void slowPrint(String message, int delay, boolean newline) {
		for (int i=0;i<message.length();i++) {
			pause(delay/2 * textSpeed);
			System.out.print(message.charAt(i));
		}
		if (newline) System.out.println();
	}

	public static void slowPrint(String message) {
		slowPrint(message,20,true);
	}

	public static void slowPrint(String message, int delay) {
		slowPrint(message,delay,true);
	}

	public static void slowPrint(String message, boolean newline) {
		slowPrint(message,20,newline);
	}

	//Get a command from the player
	private char getCommand(String valid, Scanner in) {
		char c = ' ';
		while (valid.indexOf(c) < 0) {
			System.out.print(">");
			String line = in.nextLine();
			if (line.length() > 0) {
				c = line.toLowerCase().charAt(0);
			}
		}
		return c;
	}

	//Replace stuff in a string
	private String formatString(String str) {
		return str.replace("[ENEMY]",enemy.getName()).replace("[THING]",randomItem);
	}

	private String randomString(String str) {
		String[] arr = str.split("\\|");
		int rnd = rand.nextInt(arr.length);
    	return arr[rnd];
	}

	private int rollDice(int sides) {
		return rand.nextInt(sides) + 1;
	}


}