import java.util.Random;

public class Monster {

	private String name;
	private int attack;
	private int defense;
	private int health;
	private int maxHealth;

	private String[] splashMessages;
	private String deathMessage;

	private double agression;//Has to pick a random number less than this to attack

	private String description;

	private int experienceValue;

	//Empty contructor
	public Monster() {
		name = "UNDEFINED";
		attack = 0;
		defense = 0;
		setHealth(0);
	}

	public Monster(String n, int a, int d, int h, double ag) {
		name = n;
		attack = a;
		defense = d;
		setHealth(h);
		agression = ag;
		setSplashMessages(name + " is here");
		setDeathMessage("The " + name + " has died!");
		setDescription("No description available");
	}

	public void setHealth2(int val) {
		health = val;
	}

	//Getters
	public String getName() {return name;}
	public int getHealth() {return health;}
	public int getMaxHealth() {return maxHealth;}
	public int getDefense() {return defense;}
	public int getAttack() {return attack;}
	public double getAgression() {return agression;}
	public int getExperience() {return experienceValue;}
	public String getSplashMessage() {
		int rnd = new Random().nextInt(splashMessages.length);
    	return splashMessages[rnd];
	}

	public String getDeathMessage() {return deathMessage;}
	public String getDescription() {return description;}

	public boolean isDead() {return health <= 0;}

	public boolean willAttack() {
		double chance = agression;
		return Math.random() <= chance;
	}

	public void setSplashMessages(String messages) {
		splashMessages = messages.split("\\|");
	}

	public void setDeathMessage(String messages) {
		deathMessage = messages;
	} 

	public void setDescription(String desc) {
		description = desc;
	} 

	public void setHealth(int val) {
		maxHealth = val;
		health = Math.max(maxHealth,health);

		experienceValue = (health * 2) + (attack * 2);
	}

	//Attack method, returns damage taken
	public int takeDamage(int amount, boolean defend) {
		if (defend) {
			amount = Math.max(amount-defense,0);
		}
		health -= amount;
		return amount;
	}

	public void heal(int amount) {
		health += amount;
	}

	public void levelUp() {
		attack++;
		defense++;
	}


}