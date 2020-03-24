package gc;

/**
 * @author 仇豪
 * @date 2020/3/24
 */
public class Root3 {

    /**
     * 常量s 的只想的对象不会因为a的对象被回收而被回收
     */
    public final Test s = new Test();

    public static void main(String[] args) {
        Test a = new Test();
        a = null;
    }
}
