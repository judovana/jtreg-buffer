package annotationinitialisationrace.run;

import java.util.concurrent.CountDownLatch;

import annotationinitialisationrace.domain.AnnotatedClass;
import annotationinitialisationrace.domain.Annotation;

public class IsAnnotationPresentRunnable extends SynchronisedRunnableTemplate
{

    public IsAnnotationPresentRunnable(CountDownLatch startSignal, CountDownLatch doneSignal)
    {
        super(startSignal, doneSignal);
    }

    @Override
    public void synchronisedRun()
    {
        AnnotatedClass.class.isAnnotationPresent(Annotation.class);
    }

}
