package com.gmail.dangnguyendota.client;

import com.gmail.dangnguyendota.event.stage.StagePacket;

import java.util.List;

public interface SessionListener {
    // đã kết nối tới stage server
    void onStageConnected();

    // đã ngắt kết nối tới stage server
    void onStageDisconnected();

    // một yêu cầu gửi lên stage server đã bị lỗi
    void onStageError(final int code, final String error);

    // đã kết nối tới actor server
    void onActorConnected();

    // đã ngắt kết nối với actor server
    void onActorDisconnected();

    /**
     * một yêu cầu gửi lên actor server đã bị lỗi
     *
     * @param code  mã của lỗi, phân biệt các lỗi với nhau.
     * @param error miêu tả lỗi
     */

    void onActorError(final int code, final String error);

    /**
     * lỗi trong trận đấu, lỗi này là các lỗi được gửi trong game, là các lỗi liên quan đến các
     * thao tác trong các game riêng biệt.
     *
     * @param gameId id của game tương ứng với phòng đang chơi.
     * @param roomId id của phòng.
     * @param code   mã lỗi, mã này là duy nhất đối với từng lỗi.
     * @param error  mô tả lỗi.
     */
    void onRoomError(final String gameId, final String roomId, final int code, final String error);

    /**
     * khi xảy ra một ngoại lệ trong quá trình parse packet hoặc xử lý connect.
     * @param e exception
     */
    void onException(final Exception e);

    void onStageData(final StagePacket.UserConfig config);

    void onSearchResult(final boolean ok);

    void onCancelResult(final boolean ok);

    /**
     * sau khi gửi request lên stage server, stage server sẽ gửi yêu cầu tạo phòng sang cho registrator.
     * sau đó registrator sẽ tìm một actor server phù hợp để gửi yêu cầu tạo phòng. Tạo phòng xong thì gửi trả lại
     * cho người chơi. khi đó sẽ nhảy vào hàm này.
     * @param serviceId id duy nhất của actor server.
     * @param gameId game id mà người chơi lựa chọn.
     * @param roomId id của phòng chơi.
     * @param address địa chỉ máy chủ actor .
     * @see com.gmail.dangnguyendota.event.stage.StagePacket.Room
     */
    void onCreatedRoom(final String serviceId, final String gameId, final String roomId, final String address, final StagePacket.RoomState state, final List<String> players);

    /**
     * khi một người chơi đã tham gia phòng chơi thì sẽ tất cả người
     * chơi đã tham gia phòng rồi sẽ được actor server gửi về cho một
     * playerJoined packet. Sau khi nhận được packet đó sẽ nhảy vào hàm này
     * @param id id của người chơi là UUID
     * @param name tên hiển thị của người chơi
     * @param avatar id của avatar của người chơi
     * @see com.gmail.dangnguyendota.event.actor.ActorPacket.JoinLeave
     */
    void playerJoined(final String id, final String name, final String avatar);

    /**
     * khi một người chơi đã thoát phòng thì sẽ tất cả người
     * chơi đang ở trong phòng sẽ được actor server gửi về cho một
     * playerLeft packet. Sau khi nhận được packet đó sẽ nhảy vào hàm này
     * @param id id của người chơi là UUID
     * @param name tên hiển thị của người chơi
     * @param avatar id của avatar của người chơi
     * @see com.gmail.dangnguyendota.event.actor.ActorPacket.JoinLeave
     */
    void playerLeft(final String id, final String name, final String avatar);

    /**
     * đối với authoritative game server thì trong quá trình chơi game,
     * actor server sẽ xử lý logic game và gửi trả về trạng thái
     * của trò chơi cho người chơi. Hàm này sẽ được gọi tới mỗi khi nhận được
     * gói tin đó từ actor server.
     * @param gameId id của game.
     * @param roomId id của phòng đó.
     * @param data dữ liệu server gửi về.
     * @param time thời gian mà gói tin được gửi đi.
     * @see com.gmail.dangnguyendota.event.actor.ActorPacket.RoomMessage
     */
    void onRoomMessage(final String gameId, final String roomId, final byte[] data, final long time);

    /**
     * đối với relayed game server thì mỗi khi client gửi một gói tin lên actor server
     * thì actor server sẽ gửi lại gói tin đó cho toàn bộ người chơi khác trong cùng phòng hoặc
     * tới những người chơi mà client muốn gửi tới. Hàm này sẽ được gọi đến mỗi khi một gói tin
     * realtime được gửi về.
     * @param gameId id duy nhất của game.
     * @param roomId id của phòng.
     * @param senderId id của người gửi.
     * @param data dữ liệu người gửi đã gửi.
     * @param time thời gian gói tin được gửi đi.
     * @see com.gmail.dangnguyendota.event.actor.ActorPacket.RealtimeMessages
     */
    void onRelayedMessage(final String gameId, final String roomId, final String senderId, final byte[] data, final long time);

    /**
     * khi phòng đang chơi bị đóng lại mà vẫn đang ở trong phòng thì sẽ nhận được request này.
     * @param gameId id của game đang chơi.
     * @param roomId id của phòng đang chơi.
     * @see com.gmail.dangnguyendota.event.actor.ActorPacket.ClosedRoom
     */
    void onRoomClosed(final String gameId, final String roomId);

    void onKicked(final String gameId, final String roomId);
}
