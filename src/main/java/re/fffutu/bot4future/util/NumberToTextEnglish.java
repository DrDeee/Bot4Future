package re.fffutu.bot4future.util;

/**
 * Stolen from
 * https://github.com/Joshix-1/Jemand-Bot/blob/40d0d821be59ad437d523e3eb03ee38d3d3ba425/src/main/java/Jemand/NumberToEnglischWords.java
 */
public class NumberToTextEnglish {
    private static final String[] units = {
            "",
            " one",
            " two",
            " three",
            " four",
            " five",
            " six",
            " seven",
            " eight",
            " nine"
    };
    private static final String[] doubles = {
            " ten",
            " eleven",
            " twelve",
            " thirteen",
            " fourteen",
            " fifteen",
            " sixteen",
            " seventeen",
            " eighteen",
            " nineteen"
    };
    private static final String[] tens = {
            "",
            "",
            " twenty",
            " thirty",
            " forty",
            " fifty",
            " sixty",
            " seventy",
            " eighty",
            " ninety"
    };
    private static final String[] hundreds = {
            "",
            " thousand",
            " million",
            " billion"
    };

    public static String intToText(int number) {
        StringBuilder word = new StringBuilder();
        int index = 0;
        do {
            // take 3 digits at a time
            int num = number % 1000;
            if (num != 0){
                String str = convertThreeOrLessThanThreeDigitNum(num);
                word.insert(0, str + hundreds[index]);
            }
            index++;
            number = number/1000;
        } while (number > 0);
        return word.toString().trim();
    }
    private static String convertThreeOrLessThanThreeDigitNum(int number) {
        String word = "";
        int num = number % 100;
        if (num < 10) {
            word = word + units[num];
        } else if (num < 20) {
            word = word + doubles[num % 10];
        } else {
            word = tens[num / 10] + units[num % 10];
        }
        word = (number / 100 > 0) ? units[number / 100] + " hundred" + word : word;
        return word;
    }
}
