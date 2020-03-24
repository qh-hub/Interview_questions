package gc;

/**
 * @author 仇豪
 * @date 2020/3/24
 */
public class Root1 {

    /**
     * a 是栈帧中的本地变量，当a == null
     * a 充当了GC Root的作用，a与原来指向的实例
     * new Test()断开了连接，所以对象会被回收。
     * @param args
     */
    public static void main(String[] args) {
        Test a = new Test();
        a = null;
    }
}
