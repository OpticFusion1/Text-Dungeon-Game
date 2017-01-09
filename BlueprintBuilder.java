import java.io.*;
import java.util.HashMap;

public class BlueprintBuilder {

	//Hashmap to store all the read monster blueprints
	private static HashMap<String, String> blueprints = new HashMap<String,String>();

	//Read a text file and return the results as a string
	private static String readBlueprint(String path) {

		String result = "";
		String line = " ";

		try {
			//Filereader reads the file
			FileReader fileReader = new FileReader(path);

			//Bufferedreader encases the filereader for optimization or something
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while ((line = bufferedReader.readLine()) != null) {
				if (line.length() > 0) {
					if (line.charAt(0) != '#') {
						result += line;
					}
				}
			}
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch(IOException e) {
            e.printStackTrace();
        }

		return result;
	}

	//Add a blueprint to the hashmap
	public static void addBlueprint(String path, String name) {

		String bp = readBlueprint(path);
		//If invalid blueprint, return nothing
		if (bp.length() == 0) {
			System.out.println("Error loading " + path);
		}else{
			blueprints.put(name,bp);//The monster is saved as it's file name, minus the .txt extension
		}

	}

	//Get a blueprint from the hashmap
	public static String getBlueprint(String name) {
		return blueprints.get(name);
	}

	//Parse a string value from the blueprint
	public static String blueprintParseString(String name, String propertyName) {

		String blueprint = getBlueprint(name);

		int fromIndex = blueprint.indexOf(propertyName);
		if (fromIndex < 0) return "";
		//Get string segment
		int toIndex = blueprint.indexOf(";",fromIndex);

		//Trim out the property name and assigmnet operator (=)
		String value = blueprint.substring(fromIndex,toIndex);
		int assignmentIndex = value.indexOf("=");
		value = value.substring(assignmentIndex+1);
		return value.trim();

	}

	//Parse a int value from the blueprint
	public static int blueprintParseInt(String name, String propertyName) {
		int value = -1;
		//Read value as string and convert to int, returns -1 if error
		String rawValue = blueprintParseString(name,propertyName);
		try {
			value = Integer.parseInt(rawValue);
		}catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return value;
	}

	//Parse a double value from the blueprint
	public static double blueprintParseDouble(String name, String propertyName) {
		double value = -1;
		//Read value as string and convert to int, returns -1 if error
		String rawValue = blueprintParseString(name,propertyName);
		try {
			value = Double.parseDouble(rawValue);
		}catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return value;
	}


}