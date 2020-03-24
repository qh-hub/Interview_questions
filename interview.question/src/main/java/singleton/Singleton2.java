package singleton;

/**
 * @author 仇豪
 * @date 2020/3/24
 * 懒汉
 */
public class Singleton2 {

    private volatile static Singleton2 unique;
    private Singleton2(){}
    public static synchronized Singleton2 getInstance(){
        if(unique == null){
            synchronized (Singleton2.class){
                if(unique == null){
                    unique = new Singleton2();
                }
            }
        }
        return unique;
    }
}
