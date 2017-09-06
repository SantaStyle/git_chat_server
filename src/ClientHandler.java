import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    private Socket s;
    private DataInputStream in;
    private DataOutputStream out;
    private static int CLIENT_COUNTER = 0;
    private String name;
    private MyServer owner;

    //конструктор для общения сервера и клиента
    public ClientHandler(Socket s, MyServer owner) {
        this.s = s;
        name = "";
        this.owner = owner;
        try {
            //входящий поток сообщений
            in = new DataInputStream(s.getInputStream());
            //изходящий поток сообщений
            out = new DataOutputStream(s.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //метод для получения никнейма
    public String getName() {
        return name;
    }

    //установка никнейма
    public void setName(String name) {
        this.name = name;
    }


    //метод для запуска потока
    @Override
    public void run() {
        try {
            while (true) {

                String str = null;
                //получение входящего сообщения от клиента
                str = in.readUTF();

                //поключение к серверу
                if (str != null && name.isEmpty()) {
                    String[] arr = str.split("\\s");
                    if (arr.length == 3) {
                        //получение никнейма для пользователя
                        if (arr[0].equals("/auth")) {
                            String login = arr[1];
                            String pass = arr[2];
                            String nick = SQLHandler.getNickByLoginAndPassword(login, pass);
                            if (nick != null) {
                                synchronized (owner.getClient()) {
                                    //если никнейм не занят, то пользователь авторизируется,клиент и никнейм добавляется в общий список клиентов и никнеймов,
                                    if (!owner.isNickBusy(nick)) {
                                        name = nick;
                                        sendMsg("/yes " + getName());
                                        owner.addClientnick(nick);
                                        owner.addClient(this);
                                        //для обновление активного списка клиентов на панели чата
                                        ArrayList<String> listuserArr = owner.getClientnick();
                                        String listUser = "";
                                        for (String x: listuserArr) {
                                            listUser = listUser + " " + x;
                                        }

                                        sendMsg("/listuser" + listUser);
                                        continue;
                                    } else {
                                        //отправляется сообщение клиенту что пользователь уже авторизирован
                                        sendMsg("NickBusy");
                                    }
                                }
                            } else if (nick == null) {
                                //если никнейм не найден, то отправляется сообщение "Неправильно введен логин или пароль"
                                sendMsg("stop");
                            }

                        }
                    } else if (arr.length == 4) {
                        //регистрация нового пользователя в чате
                        if (arr[0].equals("/reg")) {
                            String newlogin = arr[1];
                            String newpass = arr[2];
                            String newnick = arr[3];
                            //проверка на существование логина в БД
                            if (SQLHandler.isWrongLogin(newlogin)) {
                                sendMsg("StopUser");
                            //проверка на существование никнейма в БД
                            } else if (SQLHandler.isWrongNick(newnick)) {
                                sendMsg("StopNick");
                            //регистрация нового пользователя, если нет повторяющихся логина и никнейма
                            } else if (!SQLHandler.isWrongLogin(newlogin) & !SQLHandler.isWrongNick(newnick)) {
                                SQLHandler.addEntry(newlogin, newpass, newnick);
                            }
                        }
                    }
                }
                //отправка сообщений
                if (str != null) {
                    //отправка служебных сообщений
                    if (str.startsWith("/")) {
                        //выход из клиента в окно авторизации
                        if (str.equals("/end")) break;
                        //отправить сообщение определенному пользователю чата
                        if (str.startsWith("/pm")) {
                            String[] w = str.split("\\s");
                            String nick = w[1];
                            String msg = str.substring(w[0].length() + w[1].length() + 2);
                            if (!getName().equals(nick)) {
                                owner.sendPersonalMessage(nick, "Сообщение от " + getName() + ": " + msg);
                                sendMsg(getName() + " отправил " + nick + " : " + msg);
                            }
                        }
                        //поменять никнейм пользователю
                        if (str.startsWith("/changenick")) {
                            String[] w = str.split("\\s");
                            String nick = w[1];
                            String oldnick = getName();
                            owner.renameClientuser(nick,oldnick);
                            SQLHandler.changeNick(nick, oldnick);
                            setName(nick);
                            owner.broadCastMsg(oldnick + " поменял на " + nick);
                        }
                        continue;
                    }
                    System.out.println(name + " says: " + str);
                    owner.broadCastMsg(name + " " + str);

                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //при отключении клиента удаляются из клиенты из списка клиентов и никнеймов из списка никнеймов
            owner.removeClientuser(getName());
            owner.removeClient(this);
            //обновление активного списка пользователей в окне чата
            ArrayList<String> listuserArr = owner.getClientnick();
            String listUser = "";
            for (String x: listuserArr) {
                listUser = listUser + " " + x;
            }
            sendMsg("/listuserrem" + listUser);
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //метод для отправки сообщений клиенту
    public void sendMsg(String msg) {

        try {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
