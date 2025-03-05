package EDD;

/**
 *
 * @author B-St
 */
class OurEntry<T> {
    private int hashedKey;
    private String key;
    private T value;
    private OurEntry<T> next;

    public OurEntry(String key, int hashedKey, T value) {
        this.key = key.toLowerCase();
        this.hashedKey = hashedKey;
        this.value = value;
        this.next = null;
    }

    public int getHashedKey() {
        return hashedKey;
    }

    public String getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }

    public OurEntry<T> getNext() {
        return next;
    }

    public void setHashedKey(int hashedKey) {
        this.hashedKey = hashedKey;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public void setNext(OurEntry<T> next) {
        this.next = next;
    }


}