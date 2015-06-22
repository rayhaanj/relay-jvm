package co.fusionx.relay.message;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AndroidMessageLoop implements MessageLoop, Handler.Callback {

    private final HandlerThread mHandlerThread;
    private final Handler mHandler;

    private MessageHandler mMessageHandler;

    AndroidMessageLoop(HandlerThread handlerThread) {
        mHandlerThread = handlerThread;
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    public static MessageLoop create() {
        return new AndroidMessageLoop(new HandlerThread("co.fusionx.relay.MessageLoop"));
    }

    @Override
    public void start(@NotNull MessageHandler messageHandler) {
        mMessageHandler = messageHandler;
        mHandlerThread.start();
    }

    @Override
    public boolean isOnLoop() {
        Looper looper = mHandlerThread.getLooper();
        return looper != null && looper.equals(Looper.myLooper());
    }

    @Override
    public void post(int type, @Nullable Object obj) {
        mHandler.obtainMessage(type, obj).sendToTarget();
    }

    @Override
    public boolean handleMessage(Message msg) {
        mMessageHandler.handle(msg.what, msg.obj);
        return true;
    }
}
