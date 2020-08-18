import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;



public class WritableLongPair implements WritableComparable {

    private long l1;
    private long l2;

    /**
     * Default constructor.
     */
    public WritableLongPair() {
        l1 = 0;
        l2 = 0;
    }

    /**
     * Parameterized constructor.
     * @param l1 the first long in the pair.
     * @param l2 the second long in the pair.
     */
    public WritableLongPair(long l1, long l2) {
        this.l1 = l1;
        this.l2 = l2;
    }

    /**
     * Writes the pair to a data output.
     * @param dataOutput the data output to which to write.
     * @throws IOException
     */
    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeLong(getL1());
        dataOutput.writeLong(getL2());
    }

    /**
     * Reads a pair from a data input.
     * @param dataInput the data input from which to read.
     * @throws IOException
     */
    @Override
    public void readFields(DataInput dataInput) throws IOException {
        l1 = dataInput.readLong();
        l2 = dataInput.readLong();
    }

    /**
     *
     * @return the instance's String representation.
     */
    @Override
    public String toString() {
        return getL1() + " " + getL2();
    }

    /**
     *
     * @return the first long in the pair.
     */
    public long getL1() {
        return l1;
    }

    /**
     *
     * @return the second long in the pair.
     */
    public long getL2() {
        return l2;
    }

    /**
     *
     * @param l1 the first long to be set in the pair.
     */
    public void setL1(long l1) {
        this.l1 = l1;
    }

    /**
     *
     * @param l2 the second long to be set in the pair.
     */
    public void setL2(long l2) {
        this.l2 = l2;
    }

    /**
     *
     * @param other another instance of WritableLongPair to compare with.
     * @return 0 if the first elements in the pairs are equal; -1 if this WritableLongPair's first element is smaller;
     * 1 otherwise.
     */
    @Override
    public int compareTo(Object other){
        long ol1 = ((WritableLongPair)other).l1;
        if (l1 == ol1)
            return 0;
        else if (l1 > ol1)
            return 1;
        else return -1;
    }

}
