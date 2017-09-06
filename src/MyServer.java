import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MyServer {
    ArrayList<ClientHandler> client;
    ArrayList<String> clientnick;
    ServerSocket server = null;

    //конструктор для создание сервера
    public MyServer() {

        SQLHandler.connect();
        Socket s;
        //создаем список клиентов
        client = new ArrayList<>();
        //создаем список ников клиентов для обновления списка активных пользователей чата
        clientnick = new ArrayList<>();
        try {
            //определяем сокет для соединения клинта и сервера
            server = new ServerSocket(8189);
            while (true){

                System.out.println("Waiting for client.......");
                s = server.accept();
                System.out.println("Client connected");
                ClientHandler clientHandler = new ClientHandler(s,this);

                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                server.close();
                SQLHandler.disconnect();
            }catch (IOException e){
                e.printStackTrace();
            }

        }
    }
    //метод возвращает клиента
    public synchronized ArrayList<ClientHandler> getClient() {
        return client;
    }

    //метод для отправки сообщения определенному участнику чата
    public synchronized void sendPersonalMessage(String nick, String message){
        for(ClientHandler ch : client){
            if(ch.getName().equals(nick)){
                ch.sendMsg(message);
                break;
            }
        }
    }

    //метод определяющий занят ли никнейм
    public synchronized boolean isNickBusy(String nick){
        for (ClientHandler clientHandler: client) {
             if (clientHandler.getName().equals(nick)){
                 return true;
             }
        }
        return false;
    }

    //метод для добавления пользователя в список клиентов, а также обновление списка активных пользователей
    public synchronized void addClient(ClientHandler clientHandler){
        client.add(clientHandler);
        broadCastMsg(clientHandler.getName() + " вошел в чат");
        ArrayList<String> listuserArr = getClientnick();
        String listUser = "";
        for (String x: listuserArr) {
            listUser = listUser + " " + x;
        }
        for (ClientHandler c : client) {
            c.sendMsg("/listuser" + listUser);
        }
    }

    //метод для добавления нового никнейма при регистрации в общий список никнеймов
    public synchronized void addClientnick(String nick){
        clientnick.add(nick);

    }

    //метод для отправки сообщений всем участникам чата с указанием времени
    public synchronized void broadCastMsg(String msg){
        String str = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
        msg =  str + " " + msg;

        for (ClientHandler c : client) {
            c.sendMsg(msg);
        }
    }

    //метод для удаления пользователя из списка клиентов, а также обновление списка активных пользователей
    public synchronized void removeClient(ClientHandler clientHandler){
        client.remove(clientHandler);
        broadCastMsg(clientHandler.getName() + " вышел из чата");
        ArrayList<String> listuserArr = getClientnick();
        String listUser = "";
        for (String x: listuserArr) {
            listUser = listUser + " " + x;
        }
        for (ClientHandler c : client) {
            c.sendMsg("/listuser" + listUser);
        }

    }

    //метод для удаления никнейма из общего списка никнеймов
    public synchronized void removeClientuser(String user){
        int numberuser = clientnick.indexOf(user);
        clientnick.remove(numberuser);
    }

    //метод для получения списка никнеймов
    public synchronized ArrayList<String> getClientnick() {
        return clientnick;
    }

    //метод переименовывает никнейм, обновляется общий список никнеймов, а также обновление списка активных пользователей
    public synchronized void renameClientuser(String nick, String oldnick){
        int numberuser = clientnick.indexOf(oldnick);
        clientnick.remove(numberuser);
        clientnick.add(nick);

        ArrayList<String> listuserArr = getClientnick();
        String listUser = "";
        for (String x: listuserArr) {
            listUser = listUser + " " + x;
        }
        for (ClientHandler c : client) {
            c.sendMsg("/listuserchang" + listUser);
        }
    }
}
