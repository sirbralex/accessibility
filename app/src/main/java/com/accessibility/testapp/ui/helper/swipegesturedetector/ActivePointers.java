package com.accessibility.testapp.ui.helper.swipegesturedetector;

/**
 * Information about active pointers for gesture.
 *
 * @author Aleksandr Brazhkin
 */
class ActivePointers {
    private final int maxSize;
    private final int[] ptrIds;
    private final float[] ptrDownXs;
    private final float[] ptrDownYs;
    private final float[] ptrLastXs;
    private final float[] ptrLastYs;

    private int size = 0;

    ActivePointers(int maxSize) {
        this.maxSize = maxSize;
        this.ptrIds = new int[maxSize];
        this.ptrDownXs = new float[maxSize];
        this.ptrDownYs = new float[maxSize];
        this.ptrLastXs = new float[maxSize];
        this.ptrLastYs = new float[maxSize];
    }

    void addDownPtr(int id, float x, float y) {
        if (size == maxSize) {
            throw new IllegalStateException("No space for pointer");
        }
        ptrIds[size] = id;
        ptrDownXs[size] = x;
        ptrDownYs[size] = y;
        ptrLastXs[size] = x;
        ptrLastYs[size] = y;
        size++;
    }

    void setPtrPos(int index, float x, float y) {
        if (index > size) {
            throw new IndexOutOfBoundsException("Size is smaller");
        }
        ptrLastXs[index] = x;
        ptrLastYs[index] = y;
    }

    void deletePtrAtIndex(int index) {
        if (index > size) {
            throw new IndexOutOfBoundsException("Size is smaller");
        }
        for (int i = index; i < size - 1; i++) {
            ptrIds[i] = ptrIds[i + 1];
            ptrDownXs[i] = ptrDownXs[i + 1];
            ptrDownYs[i] = ptrDownYs[i + 1];
            ptrLastXs[i] = ptrLastXs[i + 1];
            ptrLastYs[i] = ptrLastYs[i + 1];
        }
        size--;
    }

    void clear() {
        size = 0;
    }

    int indexOfId(int id) {
        for (int i = 0; i < size; i++) {
            if (ptrIds[i] == id) {
                return i;
            }
        }
        return -1;
    }

    int getId(int index) {
        if (index > size) {
            throw new IndexOutOfBoundsException("Size is smaller");
        }
        return ptrIds[index];
    }

    float getDownX(int index) {
        if (index > size) {
            throw new IndexOutOfBoundsException("Size is smaller");
        }
        return ptrDownXs[index];
    }

    float getDownY(int index) {
        if (index > size) {
            throw new IndexOutOfBoundsException("Size is smaller");
        }
        return ptrDownYs[index];
    }

    public int size() {
        return size;
    }
}
