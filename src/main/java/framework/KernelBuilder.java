package framework;

import framework.entities.Resonator;
import framework.entities.resonatorParts.BaseResonatorPart;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class KernelBuilder {

    private static Resonator resonator;

    private static String mainKernelPartTemplate = "__kernel void %1$s(__global float* inPLUS, __global float* outPLUS,__global float* inMINUS,__global float* outMINUS){%n%2$s}";

    private static StringBuilder programSource = new StringBuilder();

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
        programSource.append(defineConstants());
        programSource.append(defineFunctions());
        programSource.append(defineMainPart());
        return programSource.toString();
    }

    private String defineConstants(){
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("__constant float maxtime = %s;\n", String.valueOf(Constants.time)));
        builder.append(String.format("__constant int numberXpoints = %s;\n", String.valueOf(Constants.resonatorPointsNumber)));
        builder.append(String.format("__constant int numberTpoints = %s;\n", String.valueOf(Constants.time)));//?
        builder.append(String.format("__constant float c = %s;\n", String.valueOf(Constants.speedOfLight)));
        builder.append(String.format("__constant float h = %s;\n", String.valueOf(Constants.planksconst)));
        builder.append(String.format("__constant float internalLoses = %s;\n", String.valueOf(Constants.internalLoses)));
        builder.append(String.format("__constant float AM_L = %s;\n", String.valueOf(Constants.time)));//?
        builder.append(String.format("__constant float AM_lifeTime = %s;\n", String.valueOf(Constants.time)));//?
        builder.append(String.format("__constant float absorption = %s;\n", String.valueOf(Constants.time)));//?
        builder.append(String.format("__constant float emission = %s;\n", String.valueOf(Constants.time)));//?
        builder.append(String.format("__constant float AM_ionsConc = %s;\n", String.valueOf(Constants.time)));//?
        builder.append(String.format("__constant float refrIndex = %s;\n", String.valueOf(Constants.refractionIndex)));
        builder.append(String.format("__constant float SA_L = %s;\n", String.valueOf(Constants.time)));//?
        builder.append(String.format("__constant float SA_lifeTime = %s;\n", String.valueOf(Constants.time)));//?
        builder.append(String.format("__constant float SA_absorbsion = %s;\n", String.valueOf(Constants.time)));//?
        builder.append(String.format("__constant float SA_emission = %s;\n", String.valueOf(Constants.time)));//?
        builder.append(String.format("__constant float SA_ionsConc = %s;\n", String.valueOf(Constants.time)));//?
        builder.append(String.format("__constant float r1 = %s;\n", String.valueOf(Constants.leftMirror)));
        builder.append(String.format("__constant float r2 = %s;\n", String.valueOf(Constants.rightMirror)));
        builder.append(String.format("__constant float R_lum = %s;\n", String.valueOf(Constants.luminiscention)));
        builder.append(String.format("__constant float pump_power = %s;\n", String.valueOf(Constants.pumpPower)));
        builder.append(String.format("__constant float pump_waveLength = %s;\n", String.valueOf(Constants.pumpWaveLength)));
        builder.append(String.format("__constant float pump_crossSection = %s;\n", String.valueOf(Constants.pumpCrossSection)));
        return builder.toString();
    }

    private String defineFunctions(){
        StringBuilder builder = new StringBuilder();
        String str =
                "float I(){return (pump_power*pump_waveLength)/(pump_crossSection*h*c);}\n" +
                "float alpha(){return ((SA_absorbsion+SA_emission)/(emission*AM_lifeTime))*SA_lifeTime*(1+absorption*AM_lifeTime*I());}\n" +
                "float koefficient_A(){/*return (AM_L*AM_ionsConc*I()*absorption*emission*AM_lifeTime)/(internalLoses*(1+absorption*AM_lifeTime*I()));*/return 4130.46;}\n" +
                "float aPLUS(float x, float u){return ((internalLoses*c)/(refrIndex*(SA_L+AM_L)))*((koefficient_A()*x - 1)+ (1-x)*(exp(-u)*R_lum));}\n" +
                "float aMINUS(float x, float u){return ((internalLoses*c)/(refrIndex*(SA_L+AM_L)))*((koefficient_A()*x - 1)+ (1-x)*(exp(-u)*R_lum));}\n" +
                "float rPLUS(float x){" +
                        "/*return (-1)*x*((c)*internalLoses)/(refrIndex*(1));//return (-1)*x*(internalLoses)/(refrIndex*(SA_L+AM_L));*/" +
                        "return (-1)*x*0.560281;}\n" +
                "float rMINUS(float x){/*return (-1)*x*(internalLoses)/(refrIndex*(SA_L+AM_L));*/return (-1)*x*0.560281;}\n" +
                "float d_AM_func(float d_AM, float uPLUS, float uMINUS, float x){return ((1/AM_lifeTime)+(absorption*((pump_power*pump_waveLength)/(pump_crossSection*h*c))*exp((-1)*absorption*AM_ionsConc*x)))*(1-(1+(exp(uPLUS) + exp(uMINUS)))*d_AM);}\n";
        builder.append(str);
        return builder.toString();
    }

    private String defineMainPart(){
        StringBuilder builder = new StringBuilder();
        String str =
                "__private float leng = AM_L + SA_L;\n" +
                "__private float dx = leng/(numberXpoints-1);\n" +
                "__private int numberAMpoints = (int)numberXpoints*AM_L/leng;\n" +
                "//__private int numberSApoints = (int)numberXpoints*SA_L/leng;\n" +
                "__private int numberSApoints = numberXpoints - numberAMpoints;\n" +
                "__private int nk = get_global_size(0);\n" +
                "__private int startX;\n" +
                "__private int endX;\n" +
                "const int numberOfX = numberXpoints;\n" +
                "__private float dt = maxtime/numberTpoints;\n" +
                "__private float middleStepPLUS[10001];\n" +
                "__private float middleStepMINUS[10001];\n" +
                "__private float tmpPlus;\n" +
                "__private float tmpMinus;\n" +
                "__local float plus[10000];\n" +
                "__local float minus[10000];\n" +
                "__local float D_AM[10000];\n" +
                "" +
                "float k1,k2,k3,k4,k5,k6,k7,k8,tmpD_AM;\n" +
                "" +
                "int num = get_global_id(0);\n" +
                "" +
                "startX = num*(numberOfX)/nk;\n" +
                "if(num != nk-1){\n" +
                "endX = (num+1)*(numberOfX)/nk;\n" +
                "} else {\n" +
                "endX = numberOfX;\n" +
                "}\n" +
                "for(int i = startX; i < endX; i++){\n" +
                "plus[i] = inPLUS[i];\n" +
                "minus[i] = inMINUS[i];\n" +
                "D_AM[i] = 0.1;\n" +
                "}\n" +
                "barrier(CLK_LOCAL_MEM_FENCE);\n" +
                "for(int t = 0;t <= numberTpoints; t++){\n" +
                "for(int i = startX; i <= endX; i++){\n" +
                "if(i == 0){\n" +
                "middleStepPLUS[i] = plus[i]+(aPLUS(D_AM[i],plus[i]) + aPLUS(D_AM[i],plus[i]))*(dt/4);\n" +
                "middleStepMINUS[i] = minus[i]+(aMINUS(D_AM[i],minus[i]) + aMINUS(D_AM[i],minus[i]))*(dt/4);\n" +
                "}else if(i == numberOfX) {\n" +
                "middleStepPLUS[i] = plus[i-1] + (aPLUS(D_AM[i-1],plus[i-1]) + aPLUS(D_AM[i-1],plus[i-1]))*(dt/4);\n" +
                "middleStepMINUS[i] = minus[i-1] + (aMINUS(D_AM[i-1],minus[i-1]) + aMINUS(D_AM[i-1],minus[i-1]))*(dt/4);\n" +
                "} else{\n" +
                "middleStepPLUS[i] = (plus[i-1] + plus[i])/2 + (rPLUS(plus[i])-rPLUS(plus[i-1]))/(2) + (aPLUS(D_AM[i-1],plus[i-1]) + aPLUS(D_AM[i],plus[i]))*(dt/4);\n" +
                "middleStepMINUS[i] = (minus[i-1] + minus[i])/2 + (rMINUS(minus[i])-rMINUS(minus[i-1]))/(2) + (aMINUS(D_AM[i-1],minus[i-1]) + aMINUS(D_AM[i],minus[i]))*(dt/4);\n" +
                "}\n" +
                "}\n" +
                "barrier(CLK_LOCAL_MEM_FENCE);\n" +
                "for(int i = startX; i < endX; i++){\n" +
                "tmpPlus = plus[i] + (rPLUS(middleStepPLUS[i+1]) - rPLUS(middleStepPLUS[i]))/c + (aPLUS(D_AM[i],plus[i]) + aPLUS(D_AM[i],plus[i]))*(dt/2);\n" +
                "tmpMinus = minus[i] + (rMINUS(middleStepMINUS[i+1]) - rMINUS(middleStepMINUS[i]))/c + (aMINUS(D_AM[i],minus[i]) + aMINUS(D_AM[i],minus[i]))*(dt/2);\n" +
                "barrier(CLK_LOCAL_MEM_FENCE);\n" +
                "plus[i] = tmpPlus;\n" +
                "minus[i] = tmpMinus;\n" +
                "k1= d_AM_func(D_AM[i],plus[i],minus[i],i*dx)*dt;\n" +
                "k2= d_AM_func(D_AM[i]+k1/5.,plus[i],minus[i],i*dx)*dt;\n" +
                "k3= d_AM_func(D_AM[i]+k1*3/40.+k2*9/40.,plus[i],minus[i],i*dx)*dt;\n" +
                "k4= d_AM_func(D_AM[i]+k1*44/45.-k2*56/15.+k3*32/9.,plus[i],minus[i],i*dx)*dt;\n" +
                "k5= d_AM_func(D_AM[i]+k1*19372/6561.-k2*25360/2187.+k3*64448/6561.-k4*212/729.,plus[i],minus[i],i*dx)*dt;\n" +
                "k6= d_AM_func(D_AM[i]+k1*9017/3168.-k2*355/33.+k3*46732/5247.+k4*49/176.-k5*5103/18656,plus[i],minus[i],i*dx)*dt;\n" +
                "k7= d_AM_func(D_AM[i]+k1*35/384.+k3*500/1113.+k4*125/192.-k5*2187/6784.+k6*11/84.,plus[i],minus[i],i*dx)*dt;\n" +
                "k8= k1*35/384.+k3*500/1113.+k4*125/192.-k5*2187/6784.+k6*11/84.;\n" +
                "tmpD_AM = D_AM[i] + k8;\n" +
                "D_AM[i] = tmpD_AM;\n" +
                "}\n" +
                "barrier(CLK_LOCAL_MEM_FENCE);\n" +
                "plus[0] = minus[numberOfX-1] + log(r1);\n" +
                "minus[0] = plus[numberOfX-1] + log(r2);\n" +
                "}\n" +
                "/*for(int i = startX; i < endX; i++){\n" +
                "outPLUS[i] = plus[i];\n" +
                "outMINUS[i] = D_AM[i];\n" +
                "}\n" +
                "barrier(CLK_GLOBAL_MEM_FENCE);*/\n";
        builder.append(String.format(mainKernelPartTemplate, Constants.kernelName, str));
        return builder.toString();
    }
}
