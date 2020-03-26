package singleton;

public enum Singleton3 {

    INSTANCE;

    public synchronized void  doSomething(){
        System.out.println("枚举方法实现单例");
    }
}
