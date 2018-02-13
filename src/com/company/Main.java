package com.company;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

public class Main {
    private static HashSet<Character> operator = new HashSet<>(Arrays.asList('+', '-', '/', '*', '^'));
    private static HashSet<Character> validChars = new HashSet<>(Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '(', ')', '.', ' '));

    public static void main(String[] args){
        try{
            newExpression();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

    private static void newExpression() throws Exception {
        BufferedReader br = new BufferedReader((new InputStreamReader(System.in)));
        System.out.println("enter expression");
        String expression = br.readLine();
        br.close();
        expression = expression.replaceAll(",", ".");
        expression = validationTest(expression);
        System.out.println("result = " + calculate(stringParse(expression)));
    }

    private static String validationTest(String calc) throws Exception {
        int counter = 0;
        boolean isBracket = false;
        boolean isDigit = false;
        for (int i = 0; i < calc.length(); i++) {
            if (Character.isDigit(calc.charAt(i))) {
                isDigit = true;
            } else if (isOperator(calc.charAt(i))) {
                isDigit = false;
            }
            if (calc.charAt(i) == ')') {
                isBracket = true;
            }
            if (isBracket) {
                if (isOperator(calc.charAt(i))) {
                    isBracket = false;
                } else if (Character.isDigit(calc.charAt(i))) {
                    throw new Exception("There is no operator between the closing parenthesisz and the number");
                }
            }
            if (validChars.contains(calc.charAt(i)) || operator.contains(calc.charAt(i))) {
                switch (calc.charAt(i)) {
                    case '(':
                        if (isDigit) {
                            throw new Exception("There is no operator between the number and the opening parenthesis");
                        } else {
                            isDigit = false;
                            counter++;
                            break;
                        }
                    case ')':
                        isBracket = true;
                        counter--;
                        break;
                    case ' ':
                        if (i == 0) {
                            break;
                        } else if (Character.isDigit(calc.charAt(i - 1)) && !isDigit) {
                            isDigit = true;
                        } else if ((i + 1 < calc.length()) && Character.isDigit(calc.charAt(i + 1)) && isDigit) {
                            throw new Exception("Missing operator");
                        }
                }
            }
        }
        if (counter != 0) {
            throw new Exception("Not equal number of parentheses");
        }
        calc = calc.replace(" ", "");
        return calc;
    }

    private static boolean isOperator(char c) {
        return operator.contains(c);
    }

    private static byte opPrior(char op) {
        switch (op) {
            case '^':
                return 3;
            case '*':
            case '/':
                return 2;
        }
        return 1;
    }

    private static int surroundMinus(StringBuilder sb, String string, int i) {
        sb.append("(0-");
        i++;
        for (int j = i; j < string.length(); j++) {
            if (isOperator(string.charAt(j))) {
                break;
            }
            sb.append(string.charAt(j));
            i++;
        }
        sb.append(")");
        if (i < string.length())
            sb.append(string.charAt(i));
        return i;
    }

    private static String stringParse(String string) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == '-') {
                if (i == 0 || string.charAt(i - 1) == ('(') || isOperator(string.charAt(i - 1))) {
                    i = surroundMinus(sb, string, i);
                }
            } else {
                sb.append(string.charAt(i));
            }
        }
        string = sb.toString();
        StringBuilder outString = new StringBuilder();
        Stack<Character> stack = new Stack<>();
        char c;
        for (int i = 0; i < string.length(); i++) {
            c = string.charAt(i);
            if (isOperator(c)) {
                for (int j = stack.size(); j > 0; j--) {
                    if (isOperator(stack.get(j - 1)) && (opPrior(c) <= opPrior(stack.get(j - 1)))) {
                        outString.append(" ").append(stack.get(j - 1));
                        stack.setSize(stack.size() - 1);
                    } else {
                        break;
                    }
                }
                stack.add(c);
                outString.append(" ");
            } else if (c == '(') {
                stack.add(c);
            } else if (c == ')') {
                for (int j = stack.size(); j > 0; j--) {
                    if (stack.get(j - 1) != '(') {
                        outString.append(" ").append(stack.get(j - 1));
                        stack.setSize(stack.size() - 1);
                    } else {
                        stack.setSize(stack.size() - 1);
                        break;
                    }
                }
            } else {
                outString.append(c);
            }
        }
        for (int i = stack.size(); i > 0; i--) {
            outString.append(" ").append(stack.get(i - 1));
            stack.setSize(stack.size() - 1);
        }
        return outString.toString();
    }

    private static BigDecimal calculate(String expression) throws Exception {
        BigDecimal result = new BigDecimal(0);
        Stack<BigDecimal> stack = new Stack<>();
        ArrayList<String> string = new ArrayList<>(Arrays.asList(expression.split(" ")));
        for (int i = 0; i < string.size(); i++) {
            if (!isOperator(string.get(i).charAt(0))) {
                stack.add(new BigDecimal(string.get(i)));
            } else {
                result = stack.get(stack.size() - 2);
                switch (string.get(i).charAt(0)) {
                    case '+':
                        result = result.add(stack.get(stack.size() - 1));
                        stack.setSize(stack.size() - 1);
                        stack.set(stack.size() - 1, result);
                        break;
                    case '-':
                        result = result.subtract(stack.get(stack.size() - 1));
                        stack.setSize(stack.size() - 1);
                        stack.set(stack.size() - 1, result);
                        break;
                    case '/':
                        if (stack.get(stack.size() - 1).compareTo(BigDecimal.ZERO) == 0) {
                            throw new Exception("Division by zero");
                        }
                        result = result.divide(stack.get(stack.size() - 1), 5, RoundingMode.HALF_EVEN);
                        stack.setSize(stack.size() - 1);
                        stack.set(stack.size() - 1, result);
                        break;
                    case '*':
                        result = result.multiply(stack.get(stack.size() - 1));
                        stack.setSize(stack.size() - 1);
                        stack.set(stack.size() - 1, result);
                        break;
                    case '^':
                        result = result.pow(stack.get(stack.size() - 1).intValueExact(), MathContext.DECIMAL32);
                        stack.set(stack.size() - 1, result);
                        break;
                    default:
                        System.out.println("Unexpected token");
                }
            }
        }
        return result;
    }
}