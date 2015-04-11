package net.bither.runnable;

public abstract class RunnableListener {
    public abstract void prepare();

    public abstract void success(Object obj);

    public abstract void error(int errorCode, String errorMsg);

    public void other(int code) {

    }
}