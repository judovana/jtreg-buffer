package annotationinitialisationrace.run;

import java.util.concurrent.CountDownLatch;

import annotationinitialisationrace.domain.Annotation;

public class GetAnnotationsRunnable extends SynchronisedRunnableTemplate
{
    
    public GetAnnotationsRunnable(CountDownLatch startSignal, CountDownLatch doneSignal)
    {
        super(startSignal, doneSignal);
    }

    @Override
    public void synchronisedRun()
    {
        Annotation.class.getAnnotations();
    }

}
