import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client implements ConnectionHandler {

    private final static Logger logger = LogManager.getLogger("clientLog");

    //0 - default, 1 - custom
    //private static InetAddress ip;
    private String nickname = "Anonymous";
    private Connection connection;
    private final int port;
    private final String ip;


    private String status = "disconnected";

    public void setStatus(String status){
        this.status = status;
    }

    public String getStatus(){
        return status;
    }



    public Connection getConnection(){
        return connection;
    }

    public static void main(String[] args) throws Exception {
        int flag = 1;
        int port = 9000;
        String ip = "localhost";
        if (args.length == 2){
            port = Integer.parseInt(args[0]);
            ip = args[1];
            flag = 0;
        }
        else if (args.length == 1){
            logger.error("Wrong amount of arguments provided: expected 0 or 2, got 1");
            System.out.println("Wrong amount of arguments provided: expected 0 or 2, got 1");
            return;
        }


        Client c = new Client(flag, ip, port);
        c.initConnection();
        c.startApplication(new InputAsker());
    }


    public Client(int flag, String ip, int port){

        this.port = port;
        this.ip = ip;
    }

    public void initConnection(){
        try {
            InetAddress ipAddr = InetAddress.getByName(this.ip);
            connection = new Connection(this, ipAddr, port);
            setStatus("connected");
        } catch (UnknownHostException e) {
            System.out.println("Wrong hostname: "+ip);
            logger.error("No host with such ip: {}", ip, e);
        } catch (IOException e) {
            logger.error(e);
        }
    }



    public void startApplication(InputAsker asker){
        //Scanner sc = new Scanner(System.in);
        System.out.println("Please provide your nickname, or press enter to stay anonymous.");
//        String name = sc.nextLine();
        try{
            String name = asker.getInput("nickname");
            if (!name.equals("")) nickname = name;
            System.out.println("Hello, "+nickname);
            logger.info("Nickname set");
            String message = "";

            while (status.equals("connected")){
                message = asker.getInput("input");
                if (message.equals("exit")){
                    logger.info("User {} requested exit. Preparing ...", this.toString());
                    connection.sendString(message);
                    connection.disconnect();
                    setStatus("disconnected");
                    logger.info("Successfully disconnected!");
//                break;
                }
                connection.sendString(nickname+": "+message);
                logger.info("User {} sent message", this.toString());
            }
        }catch (IOException e){
            System.out.println("Exception while getting the message. Terminating ...");
        }
        finally {
            connection.disconnect();
            setStatus("disconnected");
            logger.info("Successfully disconnected!");
        }


//        while (status.equals("connected")){
//            message = sc.nextLine();
//            if (message.equals("exit")){
//                logger.info("User {} requested exit. Preparing ...", this.toString());
//                connection.sendString(message);
//                connection.disconnect();
//                setStatus("disconnected");
//                logger.info("Successfully disconnected!");
////                break;
//            }
//            connection.sendString(nickname+": "+message);
//            logger.info("User {} sent message", this.toString());
//        }

        //sc.close();
    }


    public void onConnectionReady(Connection connection) {
        logger.info("Connection {} with server has been established", connection);

        System.out.println("Connection Ready");

    }


    public void onReceiveString(Connection connection, String value) {

        System.out.println(value);
    }


    public void onDisconnect(Connection connection) {
        logger.info("Connection {} id closed", connection);
        System.out.println("Connection close");

    }


    public void onException(Connection connection, Exception e) {
        logger.error("Exception on connection {} ", connection, e);
        System.out.println();
    }

//    private void printMessage(String msg){
//        System.out.println(msg);
//    }
}