import com.gmail.dangnguyendota.client.Config;
import com.gmail.dangnguyendota.example.ExampleClient;

public class ClientTest {

    public static void main(String[] args) throws Exception {
        Config config = new Config().
                withApiVersion("v1").
                withApiAddress("http://localhost:1889").
                withGameServerAddress("ws://localhost:9000/ws");
        ExampleClient exampleClient = new ExampleClient(config);
        exampleClient.start();
    }
}