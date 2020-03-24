package singleton;

/**
 * @author 仇豪
 * @date 2020/3/24
 * 饱汉
 */
public class Singleton1 {
    /**
     * 私有化构造方法
     */
    private Singleton1(){}

    private static Singleton1 unique = new Singleton1();

    public static Singleton1 Singleton1(){
        return unique;
    }

}
