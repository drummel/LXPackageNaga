package naga;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.pattern.LXPattern;

import java.util.Random;

public class SparkleTrail extends LXPattern {

    final CompoundParameter intensity = new CompoundParameter("Intensity", 2, 0.5, 10)
            .setDescription("Controls the density of sparkles near the pulse point");

    final CompoundParameter speed = new CompoundParameter("Speed", 0.5, -2, 2)
            .setDescription("Controls the speed and direction of the pulse moving down the path");

    final CompoundParameter density = new CompoundParameter("Density", 0.5, 0, 1)
            .setDescription("Controls the overall density of sparkles");

    final CompoundParameter length = new CompoundParameter("Length", 10, 1, 50)
            .setDescription("Controls the length of the sparkle trail");

    final CompoundParameter sparkleRate = new CompoundParameter("SparkleRate", 0.01, 0.001, 0.1)
            .setDescription("Controls the speed and duration of the sparkling effect");

    final DiscreteParameter numColors = new DiscreteParameter("NumColors", 1, 1, 5)
            .setDescription("Controls how many colors to use for the sparkles");

    final DiscreteParameter numTrails = new DiscreteParameter("NumTrails", 1, 1, 5)
            .setDescription("Controls the number of simultaneous sparkle trails");

    final CompoundParameter hueRotation = new CompoundParameter("HueRotation", 0, 0, 1)
            .setDescription("Randomly shifts the hue of the selected color by this amount");

    final ColorParameter color1 = new ColorParameter("Color1", LXColor.hsb(30, 100, 100)) // Orange
            .setDescription("Primary color for sparkles");
    final ColorParameter color2 = new ColorParameter("Color2", LXColor.hsb(60, 100, 100)) // Yellow
            .setDescription("Secondary color for sparkles");
    final ColorParameter color3 = new ColorParameter("Color3", LXColor.hsb(0, 100, 100)) // Red
            .setDescription("Third color for sparkles");
    final ColorParameter color4 = new ColorParameter("Color4", LXColor.hsb(20, 100, 100)) // Warm Orange
            .setDescription("Fourth color for sparkles");
    final ColorParameter color5 = new ColorParameter("Color5", LXColor.hsb(10, 100, 100)) // Deep Red
            .setDescription("Fifth color for sparkles");

    private final Random random = new Random();
    private final float[] pulsePositions;
    private final double[] sparkleTimers;
    private final float[][] sparkleDurations;

    public SparkleTrail(LX lx) {
        super(lx);
        pulsePositions = new float[5]; // Maximum of 5 trails
        sparkleTimers = new double[5];
        sparkleDurations = new float[5][model.size];

        addParameter("intensity", this.intensity);
        addParameter("speed", this.speed);
        addParameter("density", this.density);
        addParameter("length", this.length);
        addParameter("sparkleRate", this.sparkleRate);
        addParameter("numColors", this.numColors);
        addParameter("numTrails", this.numTrails);
        addParameter("hueRotation", this.hueRotation);
        addParameter("color1", this.color1);
        addParameter("color2", this.color2);
        addParameter("color3", this.color3);
        addParameter("color4", this.color4);
        addParameter("color5", this.color5);
    }

    @Override
    public void run(double deltaMs) {
        int colorsToUse = numColors.getValuei();
        int trailsToUse = numTrails.getValuei();

        for (int t = 0; t < trailsToUse; t++) {
            pulsePositions[t] += speed.getValuef() * deltaMs / 1000.0f * model.size;
            pulsePositions[t] = (pulsePositions[t] + model.size) % model.size; // Ensure position wraps around

            sparkleTimers[t] += deltaMs * sparkleRate.getValuef();

            for (int i = 0; i < model.size; i++) {
                float distanceFromPulse = Math.abs(pulsePositions[t] - i);
                if (distanceFromPulse < length.getValuef()) {
                    float brightness = Math.max(0, 1.0f - (distanceFromPulse / length.getValuef()));

                    // Determine if this point should sparkle and for how long
                    if (sparkleDurations[t][i] <= 0 && random.nextFloat() < density.getValuef() * brightness) {
                        colors[i] = LXColor.scaleBrightness(applyHueRotation(getRandomColor(colorsToUse)),
                                intensity.getValuef() * brightness);
                        sparkleDurations[t][i] = (float) (1 / sparkleRate.getValuef()); // Set duration of the sparkle
                    } else if (sparkleDurations[t][i] > 0) {
                        sparkleDurations[t][i] -= deltaMs;
                        colors[i] = LXColor.scaleBrightness(applyHueRotation(getRandomColor(colorsToUse)),
                                intensity.getValuef() * brightness);
                    } else {
                        colors[i] = LX.hsb(0, 0, 0); // Ensure transparency, allowing for gaps
                    }
                } else {
                    colors[i] = LX.hsb(0, 0, 0); // Ensure transparency for areas beyond the trail length
                    sparkleDurations[t][i] = 0;
                }
            }

            if (sparkleTimers[t] >= 1) {
                sparkleTimers[t] = 0; // Reset sparkle timer after each cycle
            }
        }
    }

    private int getRandomColor(int colorsToUse) {
        switch (random.nextInt(colorsToUse)) {
            case 0:
                return color1.getColor();
            case 1:
                return color2.getColor();
            case 2:
                return color3.getColor();
            case 3:
                return color4.getColor();
            case 4:
                return color5.getColor();
            default:
                return LXColor.BLACK;
        }
    }

    private int applyHueRotation(int color) {
        float hueRotationAmount = hueRotation.getValuef();
        if (hueRotationAmount > 0) {
            float hue = LXColor.h(color);
            float saturation = LXColor.s(color);
            float brightness = LXColor.b(color);
            hue = (hue + random.nextFloat() * hueRotationAmount * 360) % 360;
            return LX.hsb(hue, saturation, brightness);
        } else {
            return color;
        }
    }
}
