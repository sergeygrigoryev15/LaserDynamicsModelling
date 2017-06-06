package kernels;

public class ClStringConstants {
    public static String mainKernelPartTemplate =
            "__kernel void %1$s(__global float* inPLUS, __global float* outPLUS,__global float* inMINUS,__global float* outMINUS){%n%2$s}";
    public static String functions =
            "float I(){return %1$s;}\n" +
                    "float alpha(){return %2$s;}\n" +
                    "float koefficient_A(){return %3$s;}\n" +
                    "float a_AM_PLUS(float x, float u){return %4$s*((koefficient_A()*x - 1)+ (1-x)*(exp(-u)*R_lum));}\n" +
                    "float a_AM_MINUS(float x, float u){return a_AM_PLUS(x, u);}\n" +
                    "float a_SA_PLUS(float x){return %5$s*x - %4$s;}\n" +
                    "float a_SA_MINUS(float x){return a_SA_PLUS(x);}\n" +
                    "float r_AM_PLUS(float x){return x*%4$s;}\n" +
                    "float r_AM_MINUS(float x){return -r_AM_PLUS(x);}\n" +
                    "float r_SA_PLUS(float x){return r_AM_PLUS(x);}\n" +
                    "float r_SA_MINUS(float x){return r_AM_MINUS(x);}\n" +
                    "float d_AM_func(float d_AM, float uPLUS, float uMINUS, float x){" +
                    "return ((1/AM_lifeTime)+(absorption*I()*exp(-absorption*AM_ionsConc*x)))*(1-(1+(exp(uPLUS) + exp(uMINUS)))*d_AM);}\n" +
                    "float d_SA_func(float d_SA, float uPLUS, float uMINUS){" +
                    "return %6$s*(-1 - (1 + alpha()*(exp(uPLUS) + exp(uMINUS)))*d_SA);}\n";
    public static final String activeMediaMiddleStep =
            "if(i == 0){\n" +
                    "middleStepPLUS[i] = plus[i]+(a_AM_PLUS(D[i],plus[i]) + a_AM_PLUS(D[i],plus[i]))*(dt/4);\n" +
                    "middleStepMINUS[i] = minus[i]+(a_AM_MINUS(D[i],minus[i]) + a_AM_MINUS(D[i],minus[i]))*(dt/4);\n" +
                    "}else if(i == numberOfX) {\n" +
                    "middleStepPLUS[i] = plus[i-1] + (a_AM_PLUS(D[i-1],plus[i-1]) + a_AM_PLUS(D[i-1],plus[i-1]))*(dt/4);\n" +
                    "middleStepMINUS[i] = minus[i-1] + (a_AM_MINUS(D[i-1],minus[i-1]) + a_AM_MINUS(D[i-1],minus[i-1]))*(dt/4);\n" +
                    "} else{\n" +
                    "middleStepPLUS[i] = (plus[i-1] + plus[i])/2 + (r_AM_PLUS(plus[i])-r_AM_PLUS(plus[i-1]))*(dt/(2*dx)) + (a_AM_PLUS(D[i-1],plus[i-1]) + a_AM_PLUS(D[i],plus[i]))*(dt/4);\n" +
                    "middleStepMINUS[i] = (minus[i-1] + minus[i])/2 + (r_AM_MINUS(minus[i])-r_AM_MINUS(minus[i-1]))*(dt/(2*dx)) + (a_AM_MINUS(D[i-1],minus[i-1]) + a_AM_MINUS(D[i],minus[i]))*(dt/4);\n" +
                    "}\n";
    public static final String saturableAbsorberMiddleStep =
            "if(i == 0){\n" +
                    "middleStepPLUS[i] = plus[i]+(a_SA_PLUS(D[i]) + a_SA_PLUS(D[i]))*(dt/4);\n" +
                    "middleStepMINUS[i] = minus[i]+(a_SA_MINUS(D[i]) + a_SA_MINUS(D[i]))*(dt/4);\n" +
                    "}else if(i == numberOfX) {\n" +
                    "middleStepPLUS[i] = plus[i-1] + (a_SA_PLUS(D[i-1]) + a_SA_PLUS(D[i-1]))*(dt/4);\n" +
                    "middleStepMINUS[i] = minus[i-1] + (a_SA_MINUS(D[i-1]) + a_SA_MINUS(D[i-1]))*(dt/4);\n" +
                    "} else{\n" +
                    "middleStepPLUS[i] = (plus[i-1] + plus[i])/2 + (r_SA_PLUS(plus[i])-r_SA_PLUS(plus[i-1]))*(dt/(2*dx)) + (a_SA_PLUS(D[i-1]) + a_SA_PLUS(D[i]))*(dt/4);\n" +
                    "middleStepMINUS[i] = (minus[i-1] + minus[i])/2 + (r_SA_MINUS(minus[i])-r_SA_MINUS(minus[i-1]))*(dt/(2*dx)) + (a_SA_MINUS(D[i-1]) + a_SA_MINUS(D[i]))*(dt/4);\n" +
                    "}\n";
    public static final String activeMediaStep =
            "tmpPlus = plus[i] + (r_AM_PLUS(middleStepPLUS[i+1]) - r_AM_PLUS(middleStepPLUS[i]))*(dt/dx) + (a_AM_PLUS(D[i],plus[i]) + a_AM_PLUS(D[i],plus[i]))*(dt/2);\n" +
                    "tmpMinus = minus[i] + (r_AM_MINUS(middleStepMINUS[i+1]) - r_AM_MINUS(middleStepMINUS[i]))*(dt/dx) + (a_AM_MINUS(D[i],minus[i]) + a_AM_MINUS(D[i],minus[i]))*(dt/2);\n";
    public static final String saturableAbsorberStep =
            "tmpPlus = plus[i] + (r_SA_PLUS(middleStepPLUS[i+1]) - r_SA_PLUS(middleStepPLUS[i]))*(dt/dx) + (a_SA_PLUS(D[i]) + a_SA_PLUS(D[i]))*(dt/2);\n" +
                    "tmpMinus = minus[i] + (r_SA_MINUS(middleStepMINUS[i+1]) - r_SA_MINUS(middleStepMINUS[i]))*(dt/dx) + (a_SA_MINUS(D[i]) + a_SA_MINUS(D[i]))*(dt/2);\n";
    public static final String vacuumStep = "";
    public static final String activeMediaInversionStep =
            "k1= d_AM_func(D[i],plus[i],minus[i],i*dx)*dt;\n" +
                    "k2= d_AM_func(D[i]+k1/5.,plus[i],minus[i],i*dx)*dt;\n" +
                    "k3= d_AM_func(D[i]+k1*3/40.+k2*9/40.,plus[i],minus[i],i*dx)*dt;\n" +
                    "k4= d_AM_func(D[i]+k1*44/45.-k2*56/15.+k3*32/9.,plus[i],minus[i],i*dx)*dt;\n" +
                    "k5= d_AM_func(D[i]+k1*19372/6561.-k2*25360/2187.+k3*64448/6561.-k4*212/729.,plus[i],minus[i],i*dx)*dt;\n" +
                    "k6= d_AM_func(D[i]+k1*9017/3168.-k2*355/33.+k3*46732/5247.+k4*49/176.-k5*5103/18656,plus[i],minus[i],i*dx)*dt;\n" +
                    "k7= d_AM_func(D[i]+k1*35/384.+k3*500/1113.+k4*125/192.-k5*2187/6784.+k6*11/84.,plus[i],minus[i],i*dx)*dt;\n";
    public static final String saturableAbsorberInversionStep =
            "k1= d_SA_func(D[i],plus[i],minus[i])*dt;\n" +
                    "k2= d_SA_func(D[i]+k1/5.,plus[i],minus[i])*dt;\n" +
                    "k3= d_SA_func(D[i]+k1*3/40.+k2*9/40.,plus[i],minus[i])*dt;\n" +
                    "k4= d_SA_func(D[i]+k1*44/45.-k2*56/15.+k3*32/9.,plus[i],minus[i])*dt;\n" +
                    "k5= d_SA_func(D[i]+k1*19372/6561.-k2*25360/2187.+k3*64448/6561.-k4*212/729.,plus[i],minus[i])*dt;\n" +
                    "k6= d_SA_func(D[i]+k1*9017/3168.-k2*355/33.+k3*46732/5247.+k4*49/176.-k5*5103/18656,plus[i],minus[i])*dt;\n" +
                    "k7= d_SA_func(D[i]+k1*35/384.+k3*500/1113.+k4*125/192.-k5*2187/6784.+k6*11/84.,plus[i],minus[i])*dt;\n";
}
