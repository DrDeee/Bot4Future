package re.fffutu.bot4future.util;

/**
 * Stolen from
 * https://github.com/Joshix-1/Jemand-Bot/blob/40d0d821be59ad437d523e3eb03ee38d3d3ba425/src/main/java/Jemand/NumberToText.java
 */
public class NumberToTextGerman {
    private static String intToText10(int x) {
        int y=x/10;
        switch(y) {
            case 1:
                return "zehn";
            case 2:
                return "zwanzig";
            case 3:
                return "dreißig";
            case 4:
                return "vierzig";
            case 5:
                return "fünfzig";
            case 6:
                return "sechzig";
            case 7:
                return "siebzig";
            case 8:
                return "achtzig";
            case 9:
                return "neunzig";
            default:
                return "FEHLER";
        }
    }

    private static String intToText100(int x,int digits) {
        int y=x%100;
        if(y==1) {
            switch(digits) {
                case 0:
                    return "eins";
                case 1:
                case 2:
                case 3:
                    return "ein";
                case 6:
                case 9:
                case 12:
                case 15:
                    if(x==1) {
                        return "eine";
                    } else {
                        return "ein";
                    }
                default:
                    return "FEHLER";
            }
        }
        if((y>=2) && (y<=19)) {
            switch(y) {
                case 2:
                    return "zwei";
                case 3:
                    return "drei";
                case 4:
                    return "vier";
                case 5:
                    return "fünf";
                case 6:
                    return "sechs";
                case 7:
                    return "sieben";
                case 8:
                    return "acht";
                case 9:
                    return "neun";
                case 10:
                    return "zehn";
                case 11:
                    return "elf";
                case 12:
                    return "zwölf";
                case 13:
                    return "dreizehn";
                case 14:
                    return "vierzehn";
                case 15:
                    return "fünfzehn";
                case 16:
                    return "sechzehn";
                case 17:
                    return "siebzehn";
                case 18:
                    return "achtzehn";
                case 19:
                    return "neunzehn";
                default:
                    return "FEHLER";
            }
        }
        if(y>=20) {
            if(y%10==0) {
                return intToText10(y);
            } else {
                return intToText100(y%10,1)+"und"+intToText10(y);
            }
        }
        return "";
    }
    private static String intToText1000(int x,int digits) {
        if(x/100==0) {
            return intToText100(x,digits);
        } else {
            return intToText100(x/100,2)+"hundert"+intToText100(x,digits);
        }
    }

    private static String intToTextDigits(int digits,boolean mz) {
        if(mz) {
            switch(digits) {
                case 0:
                    return "";
                case 3:
                    return "tausend";
                case 6:
                    return " Millionen ";
                case 9:
                    return " Milliarden ";
                case 12:
                    return " Billionen ";
                case 15:
                    return " Billiarden ";
                default:
                    return "";
            }
        } else {
            switch(digits) {
                case 3:
                    return "tausend";
                case 6:
                    return " Million ";
                case 9:
                    return " Milliarde ";
                case 12:
                    return " Billion ";
                case 15:
                    return " Billiarde ";
                default:
                    return "";
            }
        }
    }
    public static String intToText(int x) {
        int digits;
        String result;
        if(x==0) {
            return "null";
        }
        digits=0;
        result=x<0?"minus ": "";
        x = Math.abs(x);
        while(x>0) {
            result=(x%1000>0?(intToText1000(x%1000,digits)+intToTextDigits(digits,x%1000>1)):"")+result;
            x/=1000;
            digits+=3;
        }
        return result;
    }
}
