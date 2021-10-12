package creo.mod.koreanchat;

public class Converter {
    public static int typed = 0;
    public static String lastTyped = "";
    public static String init;

    public static void resetStatus() {
        Converter.typed = 0;
        Converter.lastTyped = "";
    }

    public static String getInital(String input) {
        String en = "QWERTOP";
        String kr = "ㅃㅉㄸㄲㅆㅒㅖ";
        int idx = en.indexOf(input);
        if (idx > -1) {
            typed = 1;
            lastTyped = input;
            input = kr.charAt(idx) + "";
            return input;
        }

        en = "qwertasdfgzxcv";
        kr = "ㅂㅈㄷㄱㅅㅁㄴㅇㄹㅎㅋㅌㅊㅍ";
        idx = en.indexOf(input.toLowerCase());
        if (idx > -1) {
            typed = 1;
            lastTyped = input.toLowerCase();
            input = kr.charAt(idx) + "";
            return input;
        }

        en = "yuiophjklbnm";
        kr = "ㅛㅕㅑㅐㅔㅗㅓㅏㅣㅠㅜㅡ";
        idx = en.indexOf(input.toLowerCase());
        if (idx > -1) {
            typed = 4;
            lastTyped = input.toLowerCase();
            input = kr.charAt(idx) + "";
            return input;
        }

        return null;
    }

    public static String getVowel(String input) {
        String en = "OP";
        String kr = "ㅒㅖ";
        int idx = en.indexOf(input);
        if (idx > -1) {
            typed = 2;
            lastTyped += input;
            input = kr.charAt(idx) + "";
            return input;
        }

        en = "yuiophjklbnm";
        kr = "ㅛㅕㅑㅐㅔㅗㅓㅏㅣㅠㅜㅡ";
        idx = en.indexOf(input.toLowerCase());
        if (idx > -1) {
            typed = 2;
            lastTyped += input.toLowerCase();
            input = kr.charAt(idx) + "";
            return input;
        }

        return null;
    }

    public static String getComplexVowel(String input) {
        if (lastTyped.length() == 0)
            return null;
        String en = "jklopJKL";
        int idx = en.indexOf(input);
        if (idx > -1) {
            switch (lastTyped.charAt(lastTyped.length() - 1)) {
                case 'h':
                    if (input.toLowerCase().equals("k")) {
                        lastTyped += input;
                        input = "ㅘ";
                        return input;
                    } else if (input.equals("o")) {
                        lastTyped += input;
                        input = "ㅙ";
                        return input;
                    } else if (input.toLowerCase().equals("l")) {
                        lastTyped += input;
                        input = "ㅚ";
                        return input;
                    }
                    break;
                case 'n':
                    if (input.toLowerCase().equals("j")) {
                        lastTyped += input;
                        input = "ㅝ";
                        return input;
                    } else if (input.equals("p")) {
                        lastTyped += input;
                        input = "ㅞ";
                        return input;
                    } else if (input.toLowerCase().equals("l")) {
                        lastTyped += input;
                        input = "ㅟ";
                        return input;
                    }
                    break;
                case 'm':
                    if (input.toLowerCase().equals("l")) {
                        lastTyped += input;
                        input = "ㅢ";
                        return input;
                    }
                    break;
            }
        }

        return null;
    }

    public static String getFinalConsonant(String input) {
        String en = "RT";
        String kr = "ㄲㅆ";
        int idx = en.indexOf(input);
        if (idx > -1) {
            typed = 3;
            lastTyped += input;
            input = kr.charAt(idx) + "";
            return input;
        }

        en = "qwertasdfgzxcv";
        kr = "ㅂㅈㄷㄱㅅㅁㄴㅇㄹㅎㅋㅌㅊㅍ";
        idx = en.indexOf(input);
        if (idx > -1) {
            typed = 3;
            lastTyped += input;
            input = kr.charAt(idx) + "";
            return input;
        }

        return null;
    }

    public static String getComplexConsonant(String input) {
        if (lastTyped.length() == 0)
            return null;
        String en = "twgraqx";
        int idx = en.indexOf(input);
        if (idx > -1) {
            switch (lastTyped.charAt(lastTyped.length() - 1)) {
                case 'r':
                    if (input.equals("t")) {
                        lastTyped += input;
                        input = "ㄳ";
                        return input;
                    }
                    break;
                case 's':
                    if (input.equals("w")) {
                        lastTyped += input;
                        input = "ㄵ";
                        return input;
                    } else if (input.equals("g")) {
                        lastTyped += input;
                        input = "ㄶ";
                        return input;
                    }
                    break;
                case 'f':
                    if (input.equals("r")) {
                        lastTyped += input;
                        input = "ㄺ";
                        return input;
                    } else if (input.equals("a")) {
                        lastTyped += input;
                        input = "ㄻ";
                        return input;
                    } else if (input.equals("q")) {
                        lastTyped += input;
                        input = "ㄼ";
                        return input;
                    } else if (input.equals("t")) {
                        lastTyped += input;
                        input = "ㄽ";
                        return input;
                    } else if (input.equals("x")) {
                        lastTyped += input;
                        input = "ㄾ";
                        return input;
                    } else if (input.equals("g")) {
                        lastTyped += input;
                        input = "ㅀ";
                        return input;
                    }
                    break;
                case 'q':
                    if (input.equals("t")) {
                        lastTyped += input;
                        input = "ㅄ";
                        return input;
                    }
            }
        }

        return null;
    }

    public static String getCurrentInput() {
        return engToKor(lastTyped);
    }

    static enum CodeType {
        chosung, jungsung, jongsung
    }

    static String ignoreChars = "`1234567890-=[]\\;',./~!@#$%^&*()_+{}|:\"<>? ";

    public static String engToKor(String eng) {
        StringBuffer sb = new StringBuffer();
        int initialCode = 0, medialCode = 0, finalCode = 0;
        int tempMedialCode, tempFinalCode;

        for (int i = 0; i < eng.length(); i++) {
            if (ignoreChars.indexOf(eng.substring(i, i + 1)) > -1) {
                sb.append(eng.substring(i, i + 1));
                continue;
            }
            initialCode = getCode(CodeType.chosung, eng.substring(i, i + 1));
            i++;

            tempMedialCode = getDoubleMedial(i, eng);

            if (tempMedialCode != -1) {
                medialCode = tempMedialCode;
                i += 2;
            } else {
                medialCode = getSingleMedial(i, eng);
                i++;
            }

            tempFinalCode = getDoubleFinal(i, eng);
            if (tempFinalCode != -1) {
                finalCode = tempFinalCode;

                tempMedialCode = getSingleMedial(i + 2, eng);
                if (tempMedialCode != -1) {
                    finalCode = getSingleFinal(i, eng);
                } else {
                    i++;
                }
            } else {
                tempMedialCode = getSingleMedial(i + 1, eng);
                if (tempMedialCode != -1) {
                    finalCode = 0;
                    i--;
                } else {
                    finalCode = getSingleFinal(i, eng);
                    if (finalCode == -1) {
                        finalCode = 0;
                        i--;

                    }
                }
            }
            sb.append((char) (0xAC00 + initialCode + medialCode + finalCode));
        }
        return sb.toString();
    }

    static private int getCode(CodeType type, String c) {
        String init = "rRseEfaqQtTdwWczxvg";
        String[] mid = { "k", "o", "i", "O", "j", "p", "u", "P", "h", "hk", "ho", "hl", "y", "n", "nj", "np", "nl", "b",
                "m", "ml", "l" };
        String[] fin = { "r", "R", "rt", "s", "sw", "sg", "e", "f", "fr", "fa", "fq", "ft", "fx", "fv", "fg", "a", "q",
                "qt", "t", "T", "d", "w", "c", "z", "x", "v", "g" };

        switch (type) {
            case chosung:
                int index = init.indexOf(c);
                if (index != -1) {
                    return index * 21 * 28;
                }
                break;
            case jungsung:

                for (int i = 0; i < mid.length; i++) {
                    if (mid[i].equals(c)) {
                        return i * 28;
                    }
                }
                break;
            case jongsung:
                for (int i = 0; i < fin.length; i++) {
                    if (fin[i].equals(c)) {
                        return i + 1;
                    }
                }
                break;
            default:
                System.out.println("잘못된 타입 입니다");
        }

        return -1;
    }

    static private int getSingleMedial(int i, String eng) {
        if ((i + 1) <= eng.length()) {
            return getCode(CodeType.jungsung, eng.substring(i, i + 1));
        } else {
            return -1;
        }
    }

    static private int getDoubleMedial(int i, String eng) {
        int result;
        if ((i + 2) > eng.length()) {
            return -1;
        } else {
            result = getCode(CodeType.jungsung, eng.substring(i, i + 2));
            if (result != -1) {
                return result;
            } else {
                return -1;
            }
        }
    }

    static private int getSingleFinal(int i, String eng) {
        if ((i + 1) <= eng.length()) {
            return getCode(CodeType.jongsung, eng.substring(i, i + 1));
        } else {
            return -1;
        }
    }

    static private int getDoubleFinal(int i, String eng) {
        if ((i + 2) > eng.length()) {
            return -1;
        } else {
            return getCode(CodeType.jongsung, eng.substring(i, i + 2));
        }
    }

    public static String deleteInput() {
        String remove = lastTyped.substring(lastTyped.length() - 1);
        lastTyped = lastTyped.substring(0, lastTyped.length() - 1);
        if (lastTyped.length() > 1) {
            switch (lastTyped.charAt(lastTyped.length() - 1)) {
                case 'y':
                case 'u':
                case 'i':
                case 'o':
                case 'p':
                case 'h':
                    if (remove.equals("k")) {
                        break;
                    } else if (remove.equals("l")) {
                        break;
                    } else if (remove.equals("o")) {
                        break;
                    }
                case 'j':
                case 'k':
                case 'l':
                case 'b':
                case 'n':
                    if (remove.equals("j")) {
                        break;
                    } else if (remove.equals("l")) {
                        break;
                    } else if (remove.equals("p")) {
                        break;
                    }
                case 'm':
                    if (remove.equals("l")) {
                        break;
                    }
                case 'O':
                case 'P':
                    typed = 2;
                    break;
                case 'r':
                    if (remove.equals("t")) {
                        break;
                    }
                case 's':
                    if (remove.equals("w")) {
                        break;
                    } else if (remove.equals("g")) {
                        break;
                    }
                case 'f':
                    if (remove.equals("r")) {
                        break;
                    } else if (remove.equals("a")) {
                        break;
                    } else if (remove.equals("q")) {
                        break;
                    } else if (remove.equals("t")) {
                        break;
                    } else if (remove.equals("x")) {
                        break;
                    } else if (remove.equals("g")) {
                        break;
                    }
                case 'q':
                    if (remove.equals("t")) {
                        break;
                    }
                default:
                    typed--;
            }
        } else
            typed = 0;
        if (lastTyped.length() == 1)
            return getInital(lastTyped);
        return engToKor(lastTyped);
    }
}
