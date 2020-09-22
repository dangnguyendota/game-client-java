import com.ndn.gameclient.*;

public class ClientTest {
    static void test() throws Exception {
        Config config = new Config().
                withApiVersion("v1").
                withApiAddress("http://localhost:1889").
                withGameServerAddress("ws://localhost:9000/ws");
        ClientListener listener = new CardListener();
        Session session = new Client(config).withListener(listener);
        if(session.register("ngocdiep1", "123", "Vũ Ngọc Diệp").get()) {
            System.out.println("registered successful!");
        }
        if (session.login("ngocdiep", "123").get()) {
            System.out.println("logged in");
        } else {
            return;
        }

        if (!session.connect().get()) {
            System.out.println("can not connect to server");
        }

        try {
            System.out.println(session.searchRoom("tic-tac-toe").get());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }try {
            System.out.println(session.searchRoom("tic-tac-toe").get());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    public static void main(String[] args) throws Exception {
        test();
    }
}