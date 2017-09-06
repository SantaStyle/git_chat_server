import java.sql.*;

public class SQLHandler {
    private static Connection cn;
    private static PreparedStatement pr;
    public static void connect(){
        try {
            //подключение к базе данных
            Class.forName("org.sqlite.JDBC");
            cn = DriverManager.getConnection("jdbc:sqlite:forChat.db");
            System.out.println("База подключена!");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    //метод отключения от базы данных
    public static void disconnect(){
        try {
        if (cn!=null && !cn.isClosed())
            cn.close();
            System.out.println("База отключена");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //метод проверяющий логин и пароль при подключении к серверу
    public static String getNickByLoginAndPassword(String login, String password){
        ResultSet rs;
        String str = null;

        try {
            pr = cn.prepareStatement("SELECT nickname FROM Main WHERE login = ? AND password = ?");
            pr.setString(1, login);
            pr.setString(2, password);
            rs = pr.executeQuery();
            while (rs.next()){
                str = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return str;
    }

    //метод меняющий в БД никнейм
    public static void changeNick(String newNick, String oldNick){
        try {
            pr = cn.prepareStatement("UPDATE Main SET nickname = ? WHERE nickname = ?;");
            pr.setString(1,newNick);
            pr.setString(2, oldNick);
            pr.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    //метод регистрирующий нового пользователя, добавление в БД
    public static void addEntry(String login, String pass, String nick){
        try {
            pr = cn.prepareStatement("INSERT INTO Main(login, password,nickname) VALUES (?,?,?);");
            pr.setString(1, login);
            pr.setString(2, pass);
            pr.setString(3,nick);
            pr.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    // метод проверяющий логин
    public static boolean isWrongLogin(String login) {
        ResultSet rs;
        String str = null;
        try {
            pr = cn.prepareStatement("SELECT * FROM Main WHERE login = ?");
            pr.setString(1, login);
            rs = pr.executeQuery();
            while (rs.next()) {
                str = rs.getString(1);
                if(str == null){
                    return false;
                }
                return !str.equals(login);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // метод проверяющий никнейм
    public static boolean isWrongNick(String nick) {
        ResultSet rs;
        String str = null;
        try {
            pr = cn.prepareStatement("SELECT * FROM Main WHERE nickname = ?");
            pr.setString(1, nick);
            rs = pr.executeQuery();
            while (rs.next()) {
                str = rs.getString(1);
                if(str == null){
                    return false;
                }
                return !str.equals(nick);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
