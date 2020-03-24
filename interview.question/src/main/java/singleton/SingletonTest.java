package singleton;

/**
 * @author 仇豪
 * @date 2020/3/24
 */
public class SingletonTest {

    public static void main(String[] args) {
        Singleton3 singleton3 = Singleton3.INSTANCE;
        singleton3.doSomething();
    }
}
