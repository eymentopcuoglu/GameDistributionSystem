import java.sql.*;
import java.util.Scanner;
import java.util.UUID;

public class GameDistributionSystem {

    private static Connection connection;

    public static void main(String[] args) {
        initializeDatabase();
        showMenu();
    }

    private static void initializeDatabase() {

        String databaseURL, userName, password;
        Scanner input = new Scanner(System.in);

        System.out.print("Please enter the database URL: ");
        databaseURL = input.nextLine();

        System.out.print("Please enter the username: ");
        userName = input.nextLine();

        System.out.print("Please enter the password: ");
        password = input.nextLine();

        try {
            connection = DriverManager.getConnection(databaseURL, userName, password);
        } catch (SQLException e) {
            System.out.println("Can not connect to the database");
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Successfully connected to the database!");
    }


    private static void showMenu() {

        String inputString;

        do {
            System.out.println("\n*** Welcome to the game distribution system ***\n1. add \"game name\"" +
                    "\n2. generatekey game_id\n3. listgames\n4. listkeys\nEnter 0 to exit");

            //Taking the first word entered by the user
            Scanner input = new Scanner(System.in);
            inputString = input.next();

            switch (inputString) {
                case "add":
                    String gameName;
                    inputString = input.nextLine().trim();

                    //Getting the game name
                    gameName = inputString.substring(inputString.indexOf("\"") + 1, inputString.lastIndexOf("\""));

                    add(gameName);
                    break;

                case "generatekey":
                    //Getting the id
                    int id = Integer.parseInt(input.next().trim());

                    generateKey(id);
                    break;

                case "listgames":
                    try {
                        //Listing the games
                        listGames();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;

                case "listkeys":
                    try {
                        //Listing the keys
                        listKeys();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    if (!inputString.equals("0"))
                        System.out.println("Wrong entry!");
            }
        } while (!inputString.equals("0"));

    }

    private static void add(String gameName) {

        PreparedStatement preparedStatement;
        int numberOfRowsAffected = 0;
        try {
            preparedStatement = connection.prepareStatement("INSERT INTO game(name, create_date) VALUES (?,?)");
            preparedStatement.setString(1, gameName);
            preparedStatement.setDate(2, new java.sql.Date(new java.util.Date().getTime()));
            numberOfRowsAffected = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (numberOfRowsAffected == 0)
            System.out.println("Failed to add!");
        else
            System.out.println("Successfully added!");
    }

    private static void generateKey(int id) {

        //Creating a random UUID and converting it to the format : XXXXX-XXXXX-XXXXX-XXXXX-XXXXX
        UUID uuid = UUID.randomUUID();
        String key = uuid.toString().replaceAll("-", "").substring(5, 30).toUpperCase();
        StringBuilder stringBuilder = new StringBuilder(key);

        for (int i = 5; i <= 23; i += 6)
            stringBuilder.insert(i, "-");

        key = stringBuilder.toString();

        //If the key already exists in the database, try to generate another one
        if (keyExists(key)) {
            generateKey(id);
            return;
        }

        //Adding the key to the database
        PreparedStatement preparedStatement;
        int numberOfRowsAffected = 0;
        try {
            preparedStatement = connection.prepareStatement("INSERT into game_copy(game_id, " +
                    "game_key) values (?,?)");
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, key);
            numberOfRowsAffected = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (numberOfRowsAffected == 0)
            System.out.println("Failed to create the game copy!");
        else
            System.out.println("Successfully created the game copy!");
    }

    private static boolean keyExists(String key) {

        Statement statement;
        ResultSet resultSet;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("select game_key from game_copy");

            //Checking whether the corresponding key exists
            while (resultSet.next()) {
                if (resultSet.getString(1).equals(key))
                    return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void listGames() throws SQLException {

        System.out.printf("\n|%-10s|%-40s|%-12s|\n", "ID", "GAME", "CREATE_DATE");

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from game");

        while (resultSet.next()) {
            System.out.printf("|%-10s|%-40s|%-12s|\n", resultSet.getString(1),
                    resultSet.getString(2), resultSet.getString(3));
        }
    }

    private static void listKeys() throws SQLException {

        System.out.printf("\n|%-10s|%-40s|%-29s|\n", "ID", "GAME_ID", "GAME_KEY");

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select game_copy.id,game.name,game_copy.game_key from game_copy" +
                " join game on game_copy.game_id = game.id order by name");

        while (resultSet.next()) {
            System.out.printf("|%-10s|%-40s|%-29s|\n", resultSet.getString(1),
                    resultSet.getString(2), resultSet.getString(3));
        }
    }
}