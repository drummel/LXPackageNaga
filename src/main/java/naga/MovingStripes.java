package naga;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.pattern.LXPattern;

public class MovingStripes extends LXPattern {

    final CompoundParameter speed = new CompoundParameter("Speed", 0, -1, 1)
            .setUnits(CompoundParameter.Units.PERCENT)
            .setDescription("Controls the speed and direction of the stripe movement")
            .setPolarity(LXParameter.Polarity.BIPOLAR);

    final CompoundParameter stripeLength = new CompoundParameter("Length", 50, 1, 200);
    final ColorParameter color1 = new ColorParameter("Color1", LXColor.RED);
    final ColorParameter color2 = new ColorParameter("Color2", LXColor.GREEN);
    final ColorParameter color3 = new ColorParameter("Color3", LXColor.BLUE);
    final ColorParameter color4 = new ColorParameter("Color4", LXColor.hsb(60, 100, 100)); // Yellow
    final ColorParameter color5 = new ColorParameter("Color5", LXColor.hsb(300, 100, 100)); // Purple

    private float zOffset = 0;

    public MovingStripes(LX lx) {
        super(lx);
        addParameter("speed", this.speed);
        addParameter("length", this.stripeLength);
        addParameter("color1", this.color1);
        addParameter("color2", this.color2);
        addParameter("color3", this.color3);
        addParameter("color4", this.color4);
        addParameter("color5", this.color5);
    }

    @Override
    public void run(double deltaMs) {
        zOffset += speed.getValuef() * deltaMs;

        for (int i = 0; i < model.size; i++) {
            float z = model.points[i].z + zOffset;
            int colorIndex = ((int) Math.floor(z / stripeLength.getValuef())) % 5;

            if (colorIndex < 0) {
                colorIndex += 5;
            }

            switch (colorIndex) {
                case 0:
                    colors[i] = color1.getColor();
                    break;
                case 1:
                    colors[i] = color2.getColor();
                    break;
                case 2:
                    colors[i] = color3.getColor();
                    break;
                case 3:
                    colors[i] = color4.getColor();
                    break;
                case 4:
                    colors[i] = color5.getColor();
                    break;
            }
        }
    }
}
