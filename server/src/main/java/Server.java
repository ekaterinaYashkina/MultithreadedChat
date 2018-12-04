import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server implements ConnectionHandler {
    private final static Logger logger = LogManager.getLogger("serverLog");
    private final static ResourceManager manager = new ResourceManager();

    public static void main(String[] args) {

        if (args.length == 1){
            manager.setDirectory(args[0]);
        }

        new Server();
    }

    private CopyOnWriteArrayList<Connection> connections = new CopyOnWriteArrayList<>();

    private Server(){
        try{
            manager.loadClass();
        }
        catch (ClassNotFoundException e){
            logger.warn("No needed classes in JAR");
        }
        try (ServerSocket socket = new ServerSocket(9000)){
            logger.info("Running.....");
            while(true){
                try{
                    new Connection(this, socket.accept());
                }catch(IOException e){
                    logger.error(e);
                }
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

//    public synchronized boolean disconnectUser(Connection connection){
//
//    }


    public synchronized void onConnectionReady(Connection connection) {
        connections.add(connection);
        logger.info("New connection "+connection+"has been established");
        logger.info("Amount of connections: {}", connections.size());
        broadcast("New user has joined the chat.", connection);
    }


    public void onReceiveString(Connection connection, String value) {

        if (value.startsWith("/")) {
            String command = value.substring(0, value.indexOf(" "));
            String[] params = value.substring(value.indexOf(" ")).split(" ");
            CommandProducer result = manager.caluculate(command);
            if (result == null) connection.sendString("No such command");
            try{
            Calculator calc = new Calculator(result, params, connection);
            Thread calculation = new Thread(calc);
            calculation.start();}
            catch (IllegalArgumentException e){
                logger.warn(e);
                connection.sendString(e.getMessage());
            }


        }
        else if (value.equals("exit")){
            connection.disconnect();
        }
        else broadcast(value, connection);
    }


    public synchronized void onDisconnect(Connection connection) {
        connections.remove(connection);
        logger.info("Connection "+connection+" is closing...");
        logger.info("Amount of connections: {}", connections.size());
        broadcast("User left the chat", connection);
    }


    public synchronized void onException(Connection connection, Exception e) {
        logger.error("Exception occurred", e);
    }

    private synchronized void broadcast(String msg, Connection listn){

        final int size = connections.size();
        logger.info("Broadcasting a new message....");

        for (int i = 0; i<size; i++) {
            if (connections.get(i)!=listn) connections.get(i).sendString(msg);
        }
        logger.info("Message broadcasted!");
    }
}

