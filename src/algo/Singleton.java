package algo;


public class Singleton {

    private static volatile Singleton connection;

    private Singleton() {

        // here we will have business logic and being called in  get instance method.
    }

    public static Singleton getInstance(){
        if (null == connection) {
            synchronized (Singleton.class) {
                if (null == connection) {
                    connection = new Singleton();
                }
            }
        }
        return connection;
    }


}
