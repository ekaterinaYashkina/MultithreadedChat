import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

public class ResourceManager {

    private String directory = "plugins";

    public String getDirectory(){
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    private HashMap<String, CommandProducer> commands = new HashMap<>();

    public void append(CommandProducer cp, String name){
        commands.put(name, cp);
    }

    public CommandProducer caluculate(String command){
        return commands.get(command);
    }

    public void loadClass() throws ClassNotFoundException {
        File pluginsDir = new File(directory);
        if (pluginsDir.exists()) {
            for (File jar : pluginsDir.listFiles()) {
                try {
                    URL[] urls = new URL[]{jar.toURL()};
                    ClassLoader loader = new URLClassLoader(urls);

                    String className = jar.toString().split(".jar")[0];
                    Class classLoad = loader.loadClass(className);
                    CommandProducer prod = (CommandProducer) classLoad.newInstance();
                    String opertaion = prod.getInvokationCommand();
                    append(prod, opertaion);
//                commands.put(opertaion, prod);
                    // Apparently its bad to use Class.newInstance, so we use
                    // newClass.getConstructor() instead

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            System.out.println("Wrong path to plugins, no support for plugins will be provided.");
        }

    }
}
