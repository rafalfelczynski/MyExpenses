package android.myexpenses;

public interface Observable {

    void registerObserver(Observer obs);

    void notifyObservers();

    void removeObserver(Observer obs);

}
