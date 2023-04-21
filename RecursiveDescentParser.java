import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecursiveDescentParser {
    private List<String> tokens;
    private int currentPosition;
    private static final String IDENTIFIER_PATTERN = "^[a-zA-Z_][a-zA-Z0-9_]*$";

    public RecursiveDescentParser(List<String> tokens) {
        this.tokens = tokens;
        this.currentPosition = 0;
    }

    private String getToken(int position) {
        if (position >= 0 && position < tokens.size()) {
            return tokens.get(position);
        }
        return null;
    }

    private boolean consumeToken(String expectedToken) {
        if (currentPosition < tokens.size() && tokens.get(currentPosition).equals(expectedToken)) {
            currentPosition++;
            return true;
        }
        return false;
    }
    
    public boolean parse() {
        boolean isValid = program();
        System.out.println("IsValid: " + isValid);
        System.out.println("Current Position: " + currentPosition);
        System.out.println("Token Count: " + tokens.size());
        return isValid;
    }
    
    private boolean program() {
        boolean success = true;
    
        while (currentPosition < tokens.size()) {
            if (!stmt()) {
                success = false;
                break; // Exit the loop when stmt() fails
            }
        }
        return success;
    }
    
    private boolean stmt() {
        if (isDataType(getToken(currentPosition))) {
            return declare();
        } else if (getToken(currentPosition).equals("if")) {
            currentPosition++; // Move to the next token after "if"
            return ifStmt();
        } else if (getToken(currentPosition).equals("while")) {
            return whileLoop();
        } else if (assign()) {
            if (currentPosition < tokens.size() && getToken(currentPosition).equals(";")) {
                currentPosition++;
            }
            return true;
        } else if (getToken(currentPosition).equals("{")) {
            return block();
        }
        return false;
    }
    
    private boolean assign() {
        if (isIdentifier(getToken(currentPosition))) {
            currentPosition++;
            if (getToken(currentPosition).equals("=")) {
                currentPosition++;
                if (expr()) {
                    if (getToken(currentPosition).equals(";")) {
                        currentPosition++;
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private boolean stmtList() {
        while (currentPosition < tokens.size()) {
            if (!stmt()) {
                break;
            }
    
            if (currentPosition < tokens.size() && getToken(currentPosition).equals(";")) {
                currentPosition++;
            }
        }
        return true;
    }
    
    private boolean whileLoop() {
        if (currentPosition < tokens.size() && getToken(currentPosition).equals("while")) {
            currentPosition++;
    
            if (currentPosition < tokens.size() && getToken(currentPosition).equals("(")) {
                currentPosition++;
    
                if (boolExpr()) {
                    if (currentPosition < tokens.size() && getToken(currentPosition).equals(")")) {
                        currentPosition++;
    
                        if (block()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private boolean ifStmt() {
        if (!consumeToken("(")) {
            return false;
        }
    
        if (!boolExpr()) {
            return false;
        }
    
        if (!consumeToken(")")) {
            return false;
        }
    
        if (!stmt()) {
            return false;
        }
    
        if (consumeToken("else")) {
            if (consumeToken("if")) { // Check for 'if' after 'else'
                return ifStmt(); // If 'if' is found, call ifStmt() recursively
            } else {
                if (!stmt()) { // If 'if' is not found after 'else', call stmt()
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean elseIfStmt() {    
        if (currentPosition >= tokens.size() || !getToken(currentPosition).equals("else")) {
            return false;
        }
    
        currentPosition++;
    
        if (currentPosition >= tokens.size() || !getToken(currentPosition).equals("if")) {
            return false;
        }
    
        currentPosition++;
    
        if (boolExpr() && getToken(currentPosition).equals("{")) {
            currentPosition++;
            if (stmtList() && getToken(currentPosition).equals("}")) {
                currentPosition++;
                return true;
            }
        }
        return false;
    }
    
    private boolean block() {
        if (currentPosition < tokens.size() && getToken(currentPosition).equals("{")) {
            currentPosition++;
    
            if (stmtList()) {
                if (currentPosition < tokens.size() && getToken(currentPosition).equals("}")) {
                    currentPosition++;
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean declare() {    
        if (!isDataType(getToken(currentPosition))) {
            return false;
        }
        currentPosition++;
    
        if (!isIdentifier(getToken(currentPosition))) {
            return false;
        }
        currentPosition++;
    
        while (consumeToken(",")) {
            if (!isIdentifier(getToken(currentPosition))) {
                return false;
            }
            currentPosition++;
        }
    
        if (consumeToken("=")) {
            if (!expr()) {
                return false;
            }
    
            while (consumeToken(",")) {
                if (!isIdentifier(getToken(currentPosition))) {
                    return false;
                }
                currentPosition++;
    
                if (!consumeToken("=")) {
                    return false;
                }
    
                if (!expr()) {
                    return false;
                }
            }
        }
    
        if (!consumeToken(";")) {
            return false;
        }
        return true;
    }
    
    private boolean isDataType(String token) {
        return token.equals("int") || token.equals("float") || token.equals("double");
    }

    private boolean isIdentifier(String token) {
        if (token == null || token.length() == 0) {
            return false;
        }
    
        char firstChar = token.charAt(0);
        if (!Character.isLetter(firstChar) && firstChar != '_') {
            return false;
        }
    
        for (int i = 1; i < token.length(); i++) {
            char ch = token.charAt(i);
            if (!Character.isLetterOrDigit(ch) && ch != '_') {
                return false;
            }
        }
    
        return true;
    }
    
    private boolean parseDelimiter() {
        if (currentPosition < tokens.size() && getToken(currentPosition).equals(",")) {
            currentPosition++;
            return true;
        }
        return false;
    }
    
    private boolean parseSemicolon() {
        if (currentPosition < tokens.size() && getToken(currentPosition).equals(";")) {
            currentPosition++; // Increment currentPosition to move forward
            return true;
        }
        return false;
    }
    
    private boolean expr() {
        if (term()) {
            while (currentPosition < tokens.size() && (getToken(currentPosition).equals("+") || getToken(currentPosition).equals("-"))) {
                currentPosition++;
                if (!term()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean term() {
        if (fact()) {
            while (currentPosition < tokens.size() && (getToken(currentPosition).equals("*") || getToken(currentPosition).equals("/") || getToken(currentPosition).equals("%"))) {
                currentPosition++;
                if (!fact()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean fact() {
        String token = getToken(currentPosition);
        if (token.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) { // ID
            currentPosition++;
            return true;
        } else if (token.matches("^\\d+$") || token.matches("^\\d+\\.\\d+$")) { // INT_LIT or FLOAT_LIT
            currentPosition++;
            return true;
        } else if (token.equals("(")) {
            currentPosition++;
            if (expr()) {
                if (currentPosition < tokens.size() && getToken(currentPosition).equals(")")) {
                    currentPosition++;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean boolExpr() {    
        if (expr()) {
            while (currentPosition < tokens.size() && (getToken(currentPosition).equals(">") || getToken(currentPosition).equals("<") || getToken(currentPosition).equals("=") || getToken(currentPosition).equals("!") || getToken(currentPosition).equals("&") || getToken(currentPosition).equals("|"))) {
                if (getToken(currentPosition).equals(">") || getToken(currentPosition).equals("<")) {
                    currentPosition++;
                    if (getToken(currentPosition).equals("=")) {
                        currentPosition++;
                    }
                    if (!expr()) {
                        return false;
                    }
                } else if (getToken(currentPosition).equals("=") || getToken(currentPosition).equals("!")) {
                    currentPosition++;
                    if (getToken(currentPosition).equals("=")) {
                        currentPosition++;
                    } else {
                        return false;
                    }
                    if (!expr()) {
                        return false;
                    }
                } else if (getToken(currentPosition).equals("&")) {
                    currentPosition++;
                    if (getToken(currentPosition).equals("&")) {
                        currentPosition++;
                        if (!boolExpr()) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else if (getToken(currentPosition).equals("|")) {
                    currentPosition++;
                    if (getToken(currentPosition).equals("|")) {
                        currentPosition++;
                        if (!boolExpr()) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    private boolean bterm() {
        if (expr()) {
            while (currentPosition < tokens.size() && (getToken(currentPosition).equals("==") || getToken(currentPosition).equals("!="))) {
                currentPosition++;
                if (!expr()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    public static void main(String[] args) {
        Scanner inputScanner = new Scanner(System.in);
        System.out.println("Please enter the path to the test file (without .txt extension):");
        String testFilePath = inputScanner.nextLine() + ".txt";
            
        List<String> tokens = new ArrayList<>();
    
        try {
            Scanner fileScanner = new Scanner(new File(testFilePath));
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                Matcher m = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*|\\d+(\\.\\d+)?|[=(),{};<>!+\\-*/%|&]|<=|>=|==|!=|\\|\\|)").matcher(line);
                while (m.find()) {
                    tokens.add(m.group());
                }
            }
            fileScanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error reading test file: " + e.getMessage());
            inputScanner.close();
            return;
        }
        RecursiveDescentParser parser = new RecursiveDescentParser(tokens);
        if (parser.parse()) {
            System.out.println("The input is in the language.");
        } else {
            System.out.println("The input is not in the language.");
        }
        inputScanner.close();
    }
}
