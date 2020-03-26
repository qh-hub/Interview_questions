package thread;

import javax.print.attribute.standard.MediaSize;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @author 仇豪
 * @date 2020/3/26
 */
public class Thread3 implements Callable {

    @Override
    public Integer call() throws Exception {
        return 1;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Thread3 thread = new Thread3();
        FutureTask<Integer> futureTask = new FutureTask<>(thread);
        new Thread(futureTask,"A").start();
        //利用FutureTask.get();获取返回值
        System.out.println(futureTask.get());
    }
}
