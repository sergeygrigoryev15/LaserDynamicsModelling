import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jocl.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.IntStream;

import static org.jocl.CL.*;

public class HostPart {

    private static final String kernelName = "sampleKernel";
    private static final String clFileName = "LaserDynamics.cl";

    private static final Logger logger = LogManager.getLogger();

    private static final int maxGlobalWorkSize = 10;
    private static final int arraysLength = 10;

    private static cl_program program;
    private static cl_kernel kernel;
    private static cl_context context;
    private static cl_command_queue commandQueue;
    private static cl_mem memObjects[] = new cl_mem[3];

    /**
     * example of the generated kernel.
     */
    private static String programSource =
            "__kernel void " + kernelName + "(__global const float *a, __global const float *b, __global float *c){" +
                    "    int gid = get_global_id(0);" +
                    "    c[gid] = a[gid] * b[gid];" +
            "}";

    /**
     * Main part of the Host Part.
     *
     * @param args Not used.
     */
    public static void main(String args[]) {

        //Resonator resonator = new ResonatorBuilder().buildResonator();

        // Create input- and output data
        float srcArrayA[] = new float[arraysLength];
        float srcArrayB[] = new float[arraysLength];
        float dstArray[] = new float[arraysLength];
        IntStream.range(0, arraysLength).forEach((i) -> {
            srcArrayA[i] = i;
            srcArrayB[i] = i;
        });
        Pointer srcA = Pointer.to(srcArrayA);
        Pointer srcB = Pointer.to(srcArrayB);
        Pointer dst = Pointer.to(dstArray);

        initialize(srcA, srcB);

        // Set the arguments for the kernel
        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memObjects[0]));
        clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(memObjects[1]));
        clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(memObjects[2]));

        // Set the work-item dimensions
        long global_work_size[] = new long[]{maxGlobalWorkSize};
        long local_work_size[] = new long[]{1};

        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, global_work_size, local_work_size, 0, null, null);

        // Read the output data
        clEnqueueReadBuffer(commandQueue, memObjects[2], CL_TRUE, 0, arraysLength * Sizeof.cl_float, dst, 0, null, null);

        //output the results to the console
        IntStream.range(0, dstArray.length).mapToDouble(i -> dstArray[i]).forEach(System.out::println);

        // Release kernel, program, and memory objects
        clReleaseMemObject(memObjects[0]);
        clReleaseMemObject(memObjects[1]);
        clReleaseMemObject(memObjects[2]);

        shutdown();
    }

    /**
     * initializes all main parts of the program as kernel, program, command queue, context etc.
     * @param pointerA pointer to the first input data array.
     * @param pointerB pointer to the second input data array.
     */
    private static void initialize(Pointer pointerA, Pointer pointerB) {
        // The platform, device type and device number that will be used
        final int platformIndex = 0;
        final long deviceType = CL_DEVICE_TYPE_ALL;
        final int deviceIndex = 0;

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);

        // Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        // Obtain a platform ID
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

        // Obtain the number of devices for the platform
        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];

        // Obtain a device ID
        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        cl_device_id device = devices[deviceIndex];

        // Create a context for the selected device
        context = clCreateContext(contextProperties, 1, new cl_device_id[]{device}, null, null, null);

        // Create a command-queue for the selected device
        commandQueue = clCreateCommandQueue(context, device, 0, null);

        // Allocate the memory objects for the input- and output data
        memObjects[0] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * arraysLength, pointerA, null);
        memObjects[1] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * arraysLength, pointerB, null);
        memObjects[2] = clCreateBuffer(context, CL_MEM_READ_WRITE, Sizeof.cl_float * arraysLength, null, null);

        /*
          Create the program from the source code or from existing kernel file
          programSource = readFile(clFileName);
         */
        program = clCreateProgramWithSource(context, 1, new String[]{programSource}, null, null);

        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        // Create the kernel
        kernel = clCreateKernel(program, kernelName, null);
    }

    /**
     * Read the contents of the file with the given name, and return it as a string.
     *
     * @param fileName The name of the file to read.
     * @return The contents of the file.
     */
    private static String readFile(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            StringBuilder sb = new StringBuilder();
            String line = null;
            while (true) {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            logger.fatal(String.format("Can't convert %s file to string : ", fileName) + e);
            return "";
        }
    }

    /**
     * Release created kernel, program, command queue and context.
     */
    private static void shutdown() {
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);
    }
}




