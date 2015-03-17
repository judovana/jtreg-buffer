package annotationinitialisationrace.run;

import java.util.concurrent.CountDownLatch;

public abstract class SynchronisedRunnableTemplate implements Runnable
{
    
    private CountDownLatch startSignal;
    
    private CountDownLatch doneSignal;
    
    public SynchronisedRunnableTemplate(CountDownLatch startSignal, CountDownLatch doneSignal)
    {
        this.startSignal = startSignal;
        this.doneSignal = doneSignal;
    }

    @Override
    public final void run()
    {
        try
        {
            startSignal.await();;
            synchronisedRun();
            doneSignal.countDown();
        }
        catch (InterruptedException e)
        {
            throw new IllegalStateException(e);
        }
    }
    
    public abstract void synchronisedRun();
    
}
