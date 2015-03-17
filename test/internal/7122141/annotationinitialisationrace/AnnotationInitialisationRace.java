package annotationinitialisationrace;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import annotationinitialisationrace.run.GetAnnotationsRunnable;
import annotationinitialisationrace.run.IsAnnotationPresentRunnable;

public class AnnotationInitialisationRace
{

    private static ExecutorService pool = Executors.newFixedThreadPool(2);

    public static void main(String[] args)
    {
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(2);
        pool.execute(new GetAnnotationsRunnable(startSignal, doneSignal));
        pool.execute(new IsAnnotationPresentRunnable(startSignal, doneSignal));
        try
        {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1L));
            startSignal.countDown();
            doneSignal.await();
        }
        catch (InterruptedException e)
        {
            throw new IllegalStateException(e);
        }
        System.exit(0);
    }

}
