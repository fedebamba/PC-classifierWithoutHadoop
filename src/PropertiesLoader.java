import java.io.InputStream;

/**
 * Created by cioni on 16/02/17.
 */
public class PropertiesLoader {
    String filename="C:\\Users\\Bamba\\Documents\\GitHub\\sentana\\src\\resources\\config.txt";
    public InputStream getPropAsStream(){
        System.out.println(getClass().getClassLoader().getResourceAsStream(filename));
        return getClass().getClassLoader().getResourceAsStream(filename);

    }
}
