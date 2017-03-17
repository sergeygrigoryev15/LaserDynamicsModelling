package framework;

import framework.entities.Resonator;
import framework.entities.resonatorParts.BaseResonatorPart;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class KernelBuilder {

    private static Resonator resonator;

    private static String programSource =
            "__kernel void sampleKernel(__global const float *a, __global const float *b, __global float *c){" +
                    "    int gid = get_global_id(0);" +
                    "    c[gid] = a[gid] * b[gid];" +
                    "}";

    public static Resonator getResonator() {
        return resonator;
    }

    public static void setResonator(Resonator resonator) {
        KernelBuilder.resonator = resonator;
    }

    public KernelBuilder(Resonator resonator) {
        setResonator(resonator);
    }

    private int[] getResonatorPartsIndexes() {
        int[] points = new int[getResonator().getParts().length - 1];
        final AtomicInteger counter = new AtomicInteger(1);
        return Arrays.stream(points).map(point -> {
            double sumLength = Arrays.stream(getResonator().getParts()).limit(counter.getAndIncrement()).mapToDouble(BaseResonatorPart::getLength).sum();
            return (int) (getResonator().getLength() * sumLength / Constants.resonatorPointsNumber - 1);
        }).toArray();
    }

    public KernelBuilder buildKernel(){

        return this;
    }

    public String get(){
        return programSource;
    }

}
