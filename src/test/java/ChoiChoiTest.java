import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChoiChoiTest {

    static <T> ListenableFuture<T> createFuture() {
        SettableFuture<T> future = SettableFuture.create();
        return future;
    }

    static ListenableFuture<String> getString() {
        return createFuture();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("A");
            }
        });
    }
}
