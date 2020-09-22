import com.ndn.gameclient.ClientListener;
import com.ndn.gameclient.Error;
import java.util.Date;
import java.util.UUID;

public class CardListener implements ClientListener {
    @Override
    public void onConnected() {
        System.out.println("connected");
    }

    @Override
    public void onDisconnected() {
        System.out.println("disconnected");
    }

    @Override
    public void onHttpError(int code, String error) {
        System.out.println("http error code: " + code + ", message: " + error);
    }

    @Override
    public void onError(Error e) {
        System.out.println("error code: " + e.getCode() + ", message: " + e.getMessage());

    }

    @Override
    public void onException(Exception e) {
        System.out.println("exception " + e.getMessage());

    }

    @Override
    public void onJoinedRoom(UUID serviceId, String gameId, UUID roomId, String address, Date createTime) {
        System.out.println("joined actor service serviceId: " + serviceId + " gameId: " + gameId + " roomId: " + roomId +
                " address: " + address + " create time: " +  createTime);
    }
}
