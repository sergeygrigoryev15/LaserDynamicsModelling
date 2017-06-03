package framework;

import framework.entities.Resonator;
import framework.entities.resonatorParts.ActiveMedia;
import framework.entities.resonatorParts.BaseResonatorPart;
import framework.entities.resonatorParts.SaturableAbsorber;
import framework.enums.ResonatorElementType;
import kernels.ClStringConstants;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static framework.Constants.*;

public class KernelBuilder {

    private static Resonator resonator;

    private static StringBuilder programSource = new StringBuilder();

    public static Resonator getResonator() {
        return resonator;
    }

    public static void setResonator(final Resonator resonator) {
        KernelBuilder.resonator = resonator;
    }

    private static ActiveMedia activeMedia;
    private static SaturableAbsorber saturableAbsorber;

    public KernelBuilder(final Resonator resonator) {
        setResonator(resonator);
    }

    private long[] getResonatorPartsIndexes() {
        long[] points = new long[getResonator().getParts().size() - 1];
        final AtomicInteger counter = new AtomicInteger(1);
        for (int i = 0; i < points.length; i++) {
            double sum = 0;
            for (BaseResonatorPart part : getResonator().getParts().values().stream().limit(counter.getAndIncrement()).collect(Collectors.toList())) {
                sum += part.getLength();
            }
            points[i] = Math.round((Constants.resonatorPointsNumber / getResonator().getLength()) * sum - 1);
        }
        return points;
    }

    public String buildKernel() {
        programSource.append(defineConstants());
        programSource.append(defineFunctions());
        programSource.append(defineMainPart(getResonatorPartsIndexes()));
        return programSource.toString();
    }

    private String defineConstants() {
        StringBuilder builder = new StringBuilder();

        activeMedia = ((ActiveMedia) getResonator().getParts().get(ResonatorElementType.ACTIVE_MEDIA));
        saturableAbsorber = ((SaturableAbsorber) getResonator().getParts().get(ResonatorElementType.SATURABLE_ABSORBER));

        //We assume that dx/dt = c/2;
        double time = Constants.time * getResonator().getLength() / speedOfLight;
        long numberTpoints = Math.round((speedOfLight / 2) * (time / getResonator().getLength()) * resonatorPointsNumber);

        builder.append(String.format("__constant float maxtime = %s;\n", String.valueOf(time)));
        builder.append(String.format("__constant int numberXpoints = %s;\n", String.valueOf(resonatorPointsNumber)));
        builder.append(String.format("__constant int numberTpoints = %s;\n", String.valueOf(numberTpoints)));
        builder.append(String.format("__constant float c = %s;\n", String.valueOf(speedOfLight)));
        builder.append(String.format("__constant float h = %s;\n", String.valueOf(planksconst)));
        builder.append(String.format("__constant float internalLoses = %s;\n", String.valueOf(internalLoses)));
        builder.append(String.format("__constant float AM_L = %s;\n", String.valueOf(activeMedia.getLength())));
        builder.append(String.format("__constant float AM_lifeTime = %s;\n", String.valueOf(activeMedia.getLifeTime())));
        builder.append(String.format("__constant float absorption = %s;\n", String.valueOf(activeMedia.getAbsorption())));
        builder.append(String.format("__constant float emission = %s;\n", String.valueOf(activeMedia.getEmission())));
        builder.append(String.format("__constant float AM_ionsConc = %s;\n", String.valueOf(activeMedia.getIonsConcentration())));
        builder.append(String.format("__constant float refrIndex = %s;\n", String.valueOf(refractionIndex)));
        builder.append(String.format("__constant float SA_L = %s;\n", String.valueOf(saturableAbsorber.getLength())));
        builder.append(String.format("__constant float SA_lifeTime = %s;\n", String.valueOf(saturableAbsorber.getLifeTime())));
        builder.append(String.format("__constant float SA_absorbsion = %s;\n", String.valueOf(saturableAbsorber.getAbsorption())));
        builder.append(String.format("__constant float SA_emission = %s;\n", String.valueOf(saturableAbsorber.getEmission())));
        builder.append(String.format("__constant float SA_ionsConc = %s;\n", String.valueOf(saturableAbsorber.getIonsConcentration())));
        builder.append(String.format("__constant float r1 = %s;\n", String.valueOf(leftMirror)));
        builder.append(String.format("__constant float r2 = %s;\n", String.valueOf(rightMirror)));
        builder.append(String.format("__constant float R_lum = %s;\n", String.valueOf(luminiscention)));
        builder.append(String.format("__constant float pump_power = %s;\n", String.valueOf(pumpPower)));
        builder.append(String.format("__constant float pump_waveLength = %s;\n", String.valueOf(pumpWaveLength)));
        builder.append(String.format("__constant float pump_crossSection = %s;\n", String.valueOf(pumpCrossSection)));
        return builder.toString();
    }

    private String defineFunctions() {
        StringBuilder builder = new StringBuilder();

        double functionI = (pumpPower * pumpWaveLength) / (pumpCrossSection * speedOfLight * planksconst);
        double functionAlpha = ((saturableAbsorber.getAbsorption() + saturableAbsorber.getEmission()) / (activeMedia.getEmission() * activeMedia.getLifeTime()))
                * saturableAbsorber.getLifeTime() * (1 + activeMedia.getAbsorption() * activeMedia.getLifeTime() * functionI);
        double koefficientA = (activeMedia.getLength() * activeMedia.getIonsConcentration() * functionI * activeMedia.getAbsorption() * activeMedia.getEmission() * activeMedia.getLifeTime())
                / (internalLoses * (1 + activeMedia.getAbsorption() * activeMedia.getLifeTime() * functionI));

        double absorberA = (saturableAbsorber.getLength() * saturableAbsorber.getAbsorption() * saturableAbsorber.getIonsConcentration() * speedOfLight)
                / (refractionIndex * (saturableAbsorber.getLength() + activeMedia.getLength()));
        double rShort = speedOfLight / (refractionIndex * (activeMedia.getLength() + saturableAbsorber.getLength()));
        double r = rShort * internalLoses;

        builder.append(String.format(ClStringConstants.functions, String.valueOf(functionI), String.valueOf(functionAlpha), String.valueOf(koefficientA),
                String.valueOf(r), String.valueOf(absorberA), String.valueOf(rShort)));
        return builder.toString();
    }

    private String getStepTemplate(final long[] points, final String strForAM, final String strForSA, final String strForV) {
        StringBuilder builder = new StringBuilder();
        ResonatorElementType[] resonatorParts = getResonator().getParts().keySet().toArray(new ResonatorElementType[0]);
        String str = "if(i>=%1$s && i<%2$s){%n %3$s}%n";
        String tmp = "";
        if (points.length == 0) {
            switch (resonatorParts[0]) {
                case ACTIVE_MEDIA:
                    builder.append(strForAM);
                    break;
                case SATURABLE_ABSORBER:
                    builder.append(strForSA);
                    break;
                case VACUUM:
                    builder.append(strForV);
                    break;
            }
        } else {
            for (int i = 0; i < points.length; i++) {
                switch (resonatorParts[i]) {
                    case ACTIVE_MEDIA:
                        tmp = strForAM;
                        break;
                    case SATURABLE_ABSORBER:
                        tmp = strForSA;
                        break;
                    case VACUUM:
                        tmp = strForV;
                        break;
                }
                if (i == 0) {
                    builder.append(String.format(str, 0, points[i], tmp));
                } else if (i == points.length - 1) {
                    builder.append(String.format(str, points[i], Constants.resonatorPointsNumber, tmp));
                } else {
                    builder.append(String.format(str, points[i - 1], points[i - 1], tmp));
                }
            }
        }
        switch (resonatorParts[points.length]) {
            case ACTIVE_MEDIA:
                tmp = strForAM;
                break;
            case SATURABLE_ABSORBER:
                tmp = strForSA;
                break;
            case VACUUM:
                tmp = strForV;
        }
        builder.append(String.format(str, points[points.length - 1], Constants.resonatorPointsNumber, tmp));
        return builder.toString();
    }

    private String getMiddleStep(final long[] points) {
        return getStepTemplate(points, ClStringConstants.activeMediaMiddleStep, ClStringConstants.saturableAbsorberMiddleStep, "");
    }

    private String getMainStep(final long[] points) {
        return getStepTemplate(points, ClStringConstants.activeMediaStep, ClStringConstants.saturableAbsorberStep, ClStringConstants.vacuumStep);
    }

    private String getInversionStep(final long[] points) {
        return getStepTemplate(points, ClStringConstants.activeMediaInversionStep, ClStringConstants.saturableAbsorberInversionStep, "");
    }

    private String defineMainPart(final long[] points) {
        StringBuilder builder = new StringBuilder();
        String str =
                "__private float leng = AM_L + SA_L;\n" +
                        "__private float dx = leng/(numberXpoints-1);\n" +
                        "__private float dt = maxtime/numberTpoints;\n" +
                        "__private int nk = get_global_size(0);\n" +
                        "__private int startX;\n" +
                        "__private int endX;\n" +
                        "const int numberOfX = numberXpoints;\n" +
                        "__private float middleStepPLUS[%2$d];\n" +
                        "__private float middleStepMINUS[%2$d];\n" +
                        "__private float tmpPlus;\n" +
                        "__private float tmpMinus;\n" +
                        "__local float plus[%1$d];\n" +
                        "__local float minus[%1$d];\n" +
                        "__local float D[%1$d];\n" +
                        "float k1,k2,k3,k4,k5,k6,k7,k8,tmpD;\n" +
                        "int num = get_global_id(0);\n" +

                        "startX = num*(numberOfX)/nk;\n" +
                        "if(num != nk-1){\n" +
                        "endX = (num+1)*(numberOfX)/nk;\n" +
                        "} else {\n" +
                        "endX = numberOfX;\n" +
                        "}\n" +

                        "for(int i = startX; i < endX; i++){\n" +
                        "plus[i] = inPLUS[i];\n" +
                        "minus[i] = inMINUS[i];\n" +
                        "D[i] = 0.1;\n" +
                        "}\n" +

                        "barrier(CLK_LOCAL_MEM_FENCE);\n" +

                        "for(int t = 0;t <= numberTpoints; t++){\n" +

                        "for(int i = startX; i <= endX; i++){\n" +

                        getMiddleStep(points) +

                        "}\n" +

                        "barrier(CLK_LOCAL_MEM_FENCE);\n" +
                        "for(int i = startX; i < endX; i++){\n" +

                        getMainStep(points) +

                        "barrier(CLK_LOCAL_MEM_FENCE);\n" +
                        "plus[i] = tmpPlus;\n" +
                        "minus[i] = tmpMinus;\n" +

                        getInversionStep(points) +
                        "k8= k1*35/384.+k3*500/1113.+k4*125/192.-k5*2187/6784.+k6*11/84.;\n" +
                        "tmpD = D[i] + k8;\n" +
                        "D[i] = tmpD;\n" +

                        "}\n" +

                        "barrier(CLK_LOCAL_MEM_FENCE);\n" +
                        "plus[0] = minus[numberOfX-1] + log(r1);\n" +
                        "minus[0] = plus[numberOfX-1] + log(r2);\n" +
                        "}\n" +

                        "for(int i = startX; i < endX; i++){\n" +
                        "outPLUS[i] = plus[i];\n" +
                        "outMINUS[i] = minus[i];\n" +
                        "}\n" +
                        "barrier(CLK_GLOBAL_MEM_FENCE);\n";
        builder.append(String.format(ClStringConstants.mainKernelPartTemplate, kernelName, String.format(str, resonatorPointsNumber, resonatorPointsNumber + 1)));
        return builder.toString();
    }
}
