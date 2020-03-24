package gc;

/**
 * @author 仇豪
 * @date 2020/3/24
 */
public class Root2 {
    public static Test s;
    /**
     * 方法区中类静态属性引用的对象
     * 当栈帧中的本地变量a == null时，我们给静态变量s 赋值了变量的
     * 引用， s再此时时静态属性的引用，充当了GC Root 的作用，
     * 它指向的对象依旧存货。
     * @param args
     */
    public static void main(String[] args) {
        Test a = new Test();
        a.s = new Test();
        a = null;
    }
}
