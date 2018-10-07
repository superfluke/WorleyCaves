package fluke.worleycaves.util;

public class MathHelper extends net.minecraft.util.math.MathHelper {
	
	public static double clampedLerp(double lowerBnd, double upperBnd, double slide)
    {
        if (slide < 0.0D)
        {
            return lowerBnd;
        }
        else
        {
            return slide > 1.0D ? upperBnd : lowerBnd + (upperBnd - lowerBnd) * slide;
        }
    }
	
}
