package ifis.skysim2.common.tools;

public class StringUtils {

    // repeats a given char
    public static String repeat(char c, int i) {
	StringBuffer out = new StringBuffer();
	for (int j = 0; j < i; j++) {
	    out.append(c);
	}
	return out.toString();
    }
}
