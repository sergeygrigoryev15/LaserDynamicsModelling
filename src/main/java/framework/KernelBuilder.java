package framework;

import framework.entities.Resonator;
import framework.entities.resonatorParts.BaseResonatorPart;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class KernelBuilder {

    private static Resonator resonator;

    private static String initialString = "__kernel void %s(__global float* inPLUS, __global float* outPLUS,__global float* inMINUS,__global float* outMINUS){\n";

    private static StringBuilder programSource = new StringBuilder(String.format(initialString, Constants.kernelName));

    public static Resonator getResonator() {
        return resonator;
    }

    public static void setResonator(final Resonator resonator) {
        KernelBuilder.resonator = resonator;
    }

    public KernelBuilder(final Resonator resonator) {
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

    public String buildKernel() {

        return programSource.toString();
    }
}
