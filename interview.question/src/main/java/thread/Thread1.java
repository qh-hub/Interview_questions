package thread;

/**
 * @author 仇豪
 * @date 2020/3/26
 */
public class Thread1 extends Thread{

    @Override
    public void run() {
        System.out.println("Thread1");
    }

    public static void main(String[] args) {
        Thread1 thread1 = new Thread1();
        thread1.start();
    }
}
