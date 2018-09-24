#include <stdio.h>
#include <jni.h>

JNIEnv* create_vm(JavaVM **jvm)
{
    JNIEnv* env;
    JavaVMInitArgs args;
    JavaVMOption options[2];
    args.version = JNI_VERSION_1_8;
    args.nOptions = 2;
    options[0].optionString = "-Djava.class.path=.";
    options[1].optionString = "-Xss2048k";
    args.options = &options[0];
    args.ignoreUnrecognized = 0;
    int rv;
    rv = JNI_CreateJavaVM(jvm, (void**)&env, &args);
    if (rv < 0) return NULL;
    return env;
}

int main(int argc, char **argv)
{
    JavaVM *jvm;
    JNIEnv *env;
    jclass foo_class;
    jmethodID test_method;
    env = create_vm(&jvm);
    if(env == NULL)
        return 1;
    foo_class = (*env)->FindClass(env, "foo");
    test_method = (*env)->GetStaticMethodID(env, foo_class, "test", "(I)V");
    (*env)->CallStaticVoidMethod(env, foo_class, test_method, 1000);
    printf ("Method test called\n");

    return 0;
}
