package com.example.sd2fa;


public class GenerateChirpSignal {
    private final int SAMPLING_Rate = 44100;
    private double[] output;

    public GenerateChirpSignal(double f0, double f1, double duration) {
        int numOfSamples = (int) (duration * SAMPLING_Rate);
        output = new double[numOfSamples];
        double[] t = linspace(0, duration, numOfSamples);
        double[] phase = chirpPhase(t, f0, f1, duration);
        for (int i = 0; i < phase.length; i++) {
            output[i] = Math.cos(phase[i]);
        }
    }

    private static double[] linspace(double min, double max, int samples) {
        double[] d = new double[samples];
        for (int i = 0; i < samples; i++) {
            d[i] = min + i * (max - min) / (samples - 1);
        }
        return d;
    }

    private static double[] chirpPhase(double[] t, double f0, double f1, double t1) {
        double beta = (f1 - f0) / t1;
        double[] t_first = multiply(t, f0);
        beta = beta * 0.5;
        double[] t_second = multiply(t, beta);
        double[] t_third = multiplyArr(t, t_second);
        double[] sum = addArr(t_first, t_third);
        double[] phase = multiply(sum, 2 * Math.PI);

        return phase;
    }

    private static double[] multiply(double[] t, double multiplier) {
        double[] output = new double[t.length];
        for (int i = 0; i < t.length; i++) {
            output[i] = t[i] * multiplier;
        }
        return output;
    }

    private static double[] multiplyArr(double[] arr1, double[] arr2) {
        double[] output = new double[arr1.length];
        for (int i = 0; i < arr1.length; i++) {
            output[i] = arr1[i] * arr2[i];
        }
        return output;
    }

    private static double[] addArr(double[] arr1, double[] arr2) {
        double[] output = new double[arr1.length];
        for (int i = 0; i < arr1.length; i++) {
            output[i] = arr1[i] + arr2[i];
        }
        return output;
    }

    public double[] getArr() {
        return output;
    }
}