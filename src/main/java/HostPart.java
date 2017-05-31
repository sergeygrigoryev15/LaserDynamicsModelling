import framework.Constants;
import framework.KernelBuilder;
import framework.ResonatorBuilder;
import framework.entities.Resonator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jocl.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.IntStream;

import static org.jocl.CL.*;

public class HostPart {

    private static final Logger logger = LogManager.getLogger();

    private static final int maxGlobalWorkSize = 10;
    private static final int arraysLength = Constants.resonatorPointsNumber;

    private static cl_program program;
    private static cl_kernel kernel;
    private static cl_context context;
    private static cl_command_queue commandQueue;
    private static cl_mem memObjects[] = new cl_mem[4];

    /**
     * Main part of the Host Part.
     *
     * @param args Not used.
     */
    public static void main(String args[]) {

        // Create input- and output data
        float inPlus[] = new float[arraysLength];
        float inMinus[] = new float[arraysLength];
        float outPlus[] = new float[arraysLength];
        float outMinus[] = new float[arraysLength];
        /*
        IntStream.range(0, arraysLength).forEach((i) -> {
            inPlus[i] = i;
            inMinus[i] = i;
        });
        */
        Pointer pntrInPlus = Pointer.to(inPlus);
        Pointer pntrInMinus = Pointer.to(inMinus);
        Pointer pntrOutPlus = Pointer.to(outPlus);
        Pointer pntrOutMinus = Pointer.to(outMinus);

        initialize(pntrInPlus, pntrInMinus);

        // Set the arguments for the kernel
        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memObjects[0]));//inPLUS
        clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(memObjects[1]));//outPLUS
        clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(memObjects[2]));//inMINUS
        clSetKernelArg(kernel, 3, Sizeof.cl_mem, Pointer.to(memObjects[3]));//outMINUS

        // Set the work-item dimensions
        long global_work_size[] = new long[]{maxGlobalWorkSize};
        long local_work_size[] = new long[]{1};

        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, global_work_size, local_work_size, 0, null, null);

        // Read the output data
        clEnqueueReadBuffer(commandQueue, memObjects[1], CL_TRUE, 0, arraysLength * Sizeof.cl_float, pntrOutPlus, 0, null, null);
        clEnqueueReadBuffer(commandQueue, memObjects[3], CL_TRUE, 0, arraysLength * Sizeof.cl_float, pntrOutMinus, 0, null, null);

        //output the results to the console
        IntStream.range(0, outPlus.length).mapToDouble(i -> outPlus[i]).forEach(System.out::println);
        IntStream.range(0, outMinus.length).mapToDouble(i -> outMinus[i]).forEach(System.out::println);

        shutdown();
    }

    /**
     * initializes all main parts of the program as kernel, program, command queue, context etc.
     *
     * @param pointerA pointer to the first input data array.
     * @param pointerB pointer to the second input data array.
     */
    private static void initialize(final Pointer pointerA, final Pointer pointerB) {
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
        memObjects[1] = clCreateBuffer(context, CL_MEM_READ_WRITE, Sizeof.cl_float * arraysLength, null, null);
        memObjects[2] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * arraysLength, pointerB, null);
        memObjects[3] = clCreateBuffer(context, CL_MEM_READ_WRITE, Sizeof.cl_float * arraysLength, null, null);

        String programSource;
        if (Constants.kernelCreator.equals(Constants.KernelCreator.CODE.name())) {
            //Create kernel file from the source code
            Resonator resonator = new ResonatorBuilder().buildResonator();
            programSource = new KernelBuilder(resonator).buildKernel();
        } else {
            //Create the program from the existing kernel file
            programSource = readFile(Constants.clFileName);
        }

        program = clCreateProgramWithSource(context, 1, new String[]{programSource}, null, null);

        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        // Create the kernel
        kernel = clCreateKernel(program, Constants.kernelName, null);
    }

    /**
     * Clearing memory that points to objects.
     *
     * @param objects objects to release.
     */
    private static void releaseObjects(final cl_mem[] objects) {
        Arrays.stream(objects).forEach(CL::clReleaseMemObject);
    }

    /**
     * Read the contents of the file with the given name, and return it as a string.
     *
     * @param fileName The name of the file to read.
     * @return The contents of the file.
     */
    private static String readFile(final String fileName) {
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
        // Release kernel, program, and memory objects
        releaseObjects(memObjects);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);
    }
}




