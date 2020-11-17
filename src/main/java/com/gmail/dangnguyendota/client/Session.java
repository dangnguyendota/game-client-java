package com.gmail.dangnguyendota.client;

import com.google.common.util.concurrent.ListenableFuture;
import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.UUID;

public interface Session {
    /**
     * mỗi khi connect tới socket server, client cần một access token để
     * socket server có thể xác thực được thông tin của client, qua đó
     * tiến hành cho phép hoặc không cho phép client connect tới nó.
     *
     * @return token của session
     */
    @CheckForNull
    @CheckReturnValue
    String authToken();

    /**
     * mỗi session sẽ có 1 listener, khi các event từ server gửi trả về
     * sẽ được gửi lại vào listener để xử lý.
     *
     * @param listener class xử lý sự kiện.
     */
    void setListener(SessionListener listener);

    /**
     * thực hiện kết nối với stage socket server.
     */
    boolean connectStage();

    /**
     * thực hiện kết nối tới actor socket server.
     *
     * @param address địa chỉ máy chủ actor
     * @param gameId  game id của phòng mình muốn join.
     * @param roomId  room id của phòng mình muốn join.
     */
    boolean connectActor(@Nonnull String address, @Nonnull String gameId, @Nonnull String roomId);

    /**
     * ngắt kết nối với stage socket server
     */
    void disconnectStage();

    /**
     * ngắt kết nối với actor socket server.
     */
    void disconnectActor();

    /**
     * tải config trên server về
     */
    void loadConfig();

    /**
     * gửi request tìm phòng lên stage server.
     *
     * @param gameId game id của phòng muốn tìm.đợi đến khi connect thành công hoặc không, trả về true nếu connect
     * thành công hoặc false nếu connect thất bại.
     */
    void search(@Nonnull String gameId);

    void cancel();

    void leave(@Nonnull String gameId, @Nonnull String roomId);

    void sendRoomData(@Nonnull String gameId, @Nonnull String roomId, @Nonnull byte[] data);
}
