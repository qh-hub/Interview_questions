package thread;

/**
 * @author 仇豪
 * @date 2020/3/26
 */
public class Thread2 implements Runnable{
    @Override
    public void run() {
        System.out.println("runnable2");
    }

    public static void main(String[] args) {
        Thread2 thread2 = new Thread2();
        Thread thread = new Thread(thread2);
        thread.start();
    }
}
