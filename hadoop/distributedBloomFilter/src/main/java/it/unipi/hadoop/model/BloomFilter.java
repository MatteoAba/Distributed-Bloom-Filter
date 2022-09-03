package it.unipi.hadoop.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.List;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.util.hash.Hash;

public class BloomFilter implements Writable, Comparable<BloomFilter>{

    /*
    m = number of bit of the bloom filter
    k = number of hash functions
    arrayBF = BitSet structure representing our bloom filter (store indexes of set positions)
     */
    private int m;
    private int k;
    private BitSet arrayBF;

    // used in deserialization phase
    public BloomFilter(){}

    public BloomFilter(int m, int k) {
        this.m = m;
        this.k = k;
        this.arrayBF = new BitSet((int) m);
    }

    public BloomFilter(BloomFilter bf){
        this.arrayBF = (BitSet) bf.arrayBF.clone();
        this.m = bf.m;
        this.k = bf.k;
    }

    // used to create a bloom filter from a list of bloom filters
    public BloomFilter(int m, int k, List<BloomFilter> arrayBFs) {
        this.m = m;
        this.k = k;
        this.arrayBF = new BitSet((int) m);
        for (BloomFilter bf : arrayBFs)
            this.arrayBF.or(bf.getArrayBF());
    }

    // compute k MURMUR_HASH for a given title and set relative bits in bloom filter
    public void add(String title){
        int index;
        for(int i=0; i<k; i++){
            index = Math.abs(Hash.getInstance(Hash.MURMUR_HASH).hash(title.getBytes(StandardCharsets.UTF_8), i)) % m;
            arrayBF.set(index,true);
        }
    }
    // return true only if it finds all the k bits, associated to the title, set
    public boolean find(String title){
        int index;
        for(int i=0; i<k; i++) {
            index = Math.abs(Hash.getInstance(Hash.MURMUR_HASH).hash(title.getBytes(StandardCharsets.UTF_8), i)) % m;
            if (arrayBF.get(index) == false)
                return false;
        }
        return true;
    }

    public void or(BitSet input){
        this.arrayBF.or(input);
    }

    @Override
    public String toString() {
        String result = "m : " + this.m + " k : " + this.k + " BloomFilter : \n";
        StringBuilder s = new StringBuilder();
        // transform BitSet representation to array boolean representation
        for( int i = 0; i < arrayBF.length();  i++ ) {
            s.append( arrayBF.get( i ) == true ? 1: 0 );
        }
        return result + s;
    }

    public int compareTo(BloomFilter o) {
        if(this == o || (this.m == o.m && this.k == o.k && this.arrayBF.equals(o.arrayBF)))
            return 0;
        return 1;
    }

    public void write(DataOutput out) throws IOException {
        out.writeInt(this.m);
        out.writeInt(this.k);
        byte[] bytes = arrayBF.toByteArray();

        out.writeInt(bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            out.writeByte(bytes[i]);
        }
    }

    public void readFields(DataInput in) throws IOException {
        this.m = in.readInt();
        this.k = in.readInt();
        int length = in.readInt();
        byte[] bytes = new byte[length];

        for (int i = 0; i < length; i++) {
            bytes[i] = in.readByte();
        }
        this.arrayBF = BitSet.valueOf(bytes);
    }

    public int getM() {
        return m;
    }

    public void setM(int m) {
        this.m = m;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public BitSet getArrayBF() {
        return this.arrayBF;
    }

    public void setArrayBF(BitSet arrayBF) {
        this.arrayBF = arrayBF;
    }
}
