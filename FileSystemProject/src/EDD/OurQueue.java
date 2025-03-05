package EDD;

/**
 *
 * @author B-St
 * @param <T>
 */
public class OurQueue<T> {

    private SimpleNode<T> pFirst;
    private SimpleNode<T> pLast;
    private int size;

    public OurQueue() {
        this.pFirst = null;
        this.pLast = null;
        this.size = 0;
    }

    public boolean isEmpty() {
        return this.pFirst == null;
    }

    public void clearQueue() {
        this.pFirst = null;
        this.pLast = null;
        this.size = 0;
    }

    public void insert(T data) {

        SimpleNode<T> newNode = new SimpleNode(data);

        if (this.isEmpty()) {
            this.pFirst = newNode;
            this.pLast = newNode;

        } else {
            this.pLast.setpNext(newNode);
            this.pLast = newNode;

        }

        this.size++;

    }

    public T pop() {
        SimpleNode<T> popped = this.pFirst;
        this.pFirst = this.pFirst.getpNext();
        this.size--;

        return popped.getData();
    }

    public boolean remove(T data) {
        if (isEmpty()) {
            return false; // La cola está vacía
        }

        // Si el primer elemento es el que queremos eliminar
        if (pFirst.getData().equals(data)) {
            pop(); // Si es el primer elemento, simplemente lo eliminamos
            return true;
        }

        // Buscamos el nodo que contiene el dato
        SimpleNode<T> current = pFirst;
        while (current.getpNext() != null) {
            if (current.getpNext().getData().equals(data)) {
                // Elimina el nodo
                current.setpNext(current.getpNext().getpNext());
                if (current.getpNext() == null) { // Si estamos eliminando el último nodo
                    pLast = current; // Actualiza pLast
                }
                size--;
                return true; // Elemento eliminado
            }
            current = current.getpNext();
        }
        return false; // No se encontró el elemento
    }

    // STILL A WIP
    public SimpleList<T> getListSortedFromQueue(OurQueue<T> queue) {
        SimpleList<T> dataList = new SimpleList<>();

        if (queue == null || queue.isEmpty()) {
            System.out.println("La cola esta vacia o no se pudo acceder a ella.");
            return dataList;
        }

        // Recorremos la cola desde el primer nodo
        SimpleNode<T> currentNode = queue.getpFirst();

        while (currentNode != null) {
            dataList.addAtTheEnd(currentNode.getData());
            currentNode = currentNode.getpNext();
        }

        return dataList;
    }

    public SimpleNode<T> getpFirst() {
        return pFirst;
    }

    public void setpFirst(SimpleNode<T> pFirst) {
        this.pFirst = pFirst;
    }

    public SimpleNode<T> getpLast() {
        return pLast;
    }

    public void setpLast(SimpleNode<T> pLast) {
        this.pLast = pLast;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

}
