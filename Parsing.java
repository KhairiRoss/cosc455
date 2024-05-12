import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Parsing {

    // Inner class representing a token
    public static class Token {
        private String kind;
        private String value;
        private String position;

        public Token(String kind, String value, String position) {
            // Update value if it's a boolean literal
            this.value = updateValue(kind, value); 
            this.kind = updateKind(kind);
            this.position = position;
        }

        // Update kind to handle boolean literals
        public String updateKind(String k) {
            if ("true".equals(k) || "false".equals(k)) {
                return "BooleanLiteral";
            } else {
                return k;
            }
        }

        // Update value for boolean literals
        public String updateValue(String k, String v) {
            if ("true".equals(k) || "false".equals(k)) {
                this.kind = "BooleanLiteral";
                return k;
            } else {
                return v;
            }
        }

        // Getters
        public String getKind() {
            return kind;
        }

        public String getValue() {
            return value;
        }

        public String getPosition() {
            return position;
        }
    }

    // Inner class representing a lexical analyzer
    public static class LexicalAnalyzer {
        private char c; // current character
        private String tokenRead; // currently read token
        private int[] pos = {1, 0}; // current position (line, line character)
        private int[] tokenPos = {1, 0}; // position of current token
        private String kind = ""; // type of current token
        private String value = ""; // value of current token
        private boolean comment = false; // keeps track if currently reading a comment
        private BufferedReader reader; // bufferedreader for input
        private boolean exit = false; // checks if we should exit the lexical analyzer or not

        // Arrays to store alphabet, digits, and symbols
        private char[] letters = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'u', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'U', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        private char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        private char[] symbols = {':', '=', '<', '>', '!', '+', '-', '*', '/', ';', ')', '(', '.', ','};
        private String[] keywords = {"program", "if", "then", "else", "while", "do", "print", "not", "or", "and", "false", "true", "bool", "int", "end", "mod"};

        // Constructor
        public LexicalAnalyzer() {
        }

        // Set file input for lexical analyzer
        public void fileInput(FileReader input) {
            reader = new BufferedReader(input);
            readNextChar(); // read first char to prepare for first next() call
        }

        // Read next character
        private void readNextChar() {
            try {
                c = (char) reader.read(); // need to convert to char, since bufferedreader returns ints values for characters
                pos[1]++; // increment character position in line
            } catch (IOException e) {
                System.out.println(position() + "\t>>>>>>IO Exception");
                exit = true; // set exit to true, for exiting program
            }
        }

        // Process the next token
        public void next() {
            //Call next from LA
            // if isComment()
            // next()
            //if LA.shouldEnd()
            //exit
            if (comment) eatComment(); // if currently reading a comment, eat until end of comment
            eatWhiteSpace(); // eat white spaces
            tokenPos[0] = pos[0]; // register first line location of token (somewhat redundant as those should not change while reading, but nice to have it in array)
            tokenPos[1] = pos[1]; // register first char location of token
            tokenRead = ""; // reset token value
            identifyType(); // identify which token we're looking at
            System.out.println("Position: " + position() + ", Token Kind: " + kind + ", Value: " + value);

        }

        // Identify the type of the current token
        private void identifyType() {
            if (c == (char) (-1)) { // if -1, we've reached end of file
                kind = "end-of-file";
                value = "";
            } else if (languageCheck(letters)) // if it is a letter, read word (for ID or keywords)
                readWord();
            else if (languageCheck(digits)) // if digit, read Num
                readNumber();
            else if (languageCheck(symbols)) // if symbol, read symbol (for operands or other terminals)
                readSymbols();
            else
                reportError(c); // otherwise, this is NOT an allowed character
            
            // Skip the first slash of a comment
            if (comment && c == '/') {
                System.out.println("Found comment");
                eatComment();
                identifyType();
            }
        }
        // Check if character is part of the given language array
        private boolean languageCheck(char[] array) {
            for (char i : array) { // loop through language array
                if (c == i) {
                    return true; // if exists in array, then it matches with alphabet
                }
            }
            return false;
        }

        // Eat characters until end of comment is reached
        private void eatComment() {
            boolean end = false;
            while (c != '\n') {// eat characters until a new line is reached
                if (c == (char) -1) {
                    end = true;
                    break;
                } else
                    readNextChar();
            }
            if (!end) {
                // reset and update vars to reflect position
                pos[0]++;
                pos[1] = 0;
                readNextChar(); // go onto next read
            }
            comment = false;

        }

        // Eat white spaces
        private void eatWhiteSpace() {
            // if current character is a new line, update position
            if (c == '\n') {
                pos[0]++;
                pos[1] = 0;
            }
            while (Character.isWhitespace(c)) { // check if character is whitespace
                readNextChar(); // go to next char to see if it is whitespace too or not
                if (c == '\n') {
                    pos[0]++;
                    pos[1] = 0;
                } // update pos if new line
            }
        }

        // Read a word (ID or keyword)
        private void readWord() {
            tokenRead += c; // append current character to current value (building up word
            boolean wordReading = true;
        
            // loop until not a word
            do {
                readNextChar(); // look at next char
                if (languageCheck(letters) || languageCheck(digits) || c == '_') { // all IDs have letters, numbers, or _
                    tokenRead += c; // append
                } else // if you find something else, end of word
                    wordReading = false;
            } while (wordReading);
        
            kind = "ID"; // tentatively set kind to ID
            value = tokenRead; // and value to what we have read
        
            // check to see if the word we've read is a keyword or not
            for (String i : keywords) {
                if (tokenRead.equals(i)) {
                    kind = tokenRead; // if it is, kind will instead equal what we have read so far
                    value = ""; // and value will be empty
                    break; // exit loop
                }
            }
        }
        
        // Read a number
        private void readNumber() {
            tokenRead += c; // append char to value
            boolean numRead = true;

            // loop until not a number
            do {
                readNextChar(); // look at next char
                if (languageCheck(digits)) // if a digit, then append
                    tokenRead += c;
                else
                    numRead = false; // otherwise end number
            } while (numRead);

            kind = "NUM";
            value = tokenRead;

        }

        // Read symbols
        private void readSymbols() {
            tokenRead += c; // append char to value
            char firstChar = c; // store the current char since we will need to compare to the next character

            readNextChar(); // look at next char

            // comparing startchar to next char
            switch (firstChar) {
                case '/':
                    if (c == '/') // if we see //, then it is a comment
                        comment = true;
                    break; // otherwise it can just be / on its own

                case '=': // can be = or =<
                    if (c == '<')
                        tokenRead += c;
                    break; // no error reported since = can stand on its own

                case '>': // can be > or >=
                    if (c == '=')
                        tokenRead += c;
                    break; // no error reported since > can stand on their own

                case ':': // can be : or :=
                    if (c == '=')
                        tokenRead += c;
                    break; // no error reported since : can stand on their own

                case '!': // we began with !
                    if (c == '=') // only != is allowed
                        tokenRead += c;
                    else // anything else is an error (since ! isnt allowed on its own)
                        reportError(firstChar);
                    break;


                // and other symbols exist only on their own
            }

            kind = tokenRead; // for symbols, kind = what we have read
            value = ""; // and value is empty

            if (kind.length() > 1) // if we only saw one symbol by itself, we don't need to read another character, since the next character was already read
                readNextChar(); // read next char for next() setup

        }

        // Getters

        public String kind() {
            return kind;
        }

        public String stringValue() {
            return value;
        }

        public int numValue() {
            return Integer.parseInt(value);
        }

        public String position() {
            return tokenPos[0] + ":" + tokenPos[1];
        }

        // Other methods utilized by parser

        public boolean isNumber() {
            return kind.equals("NUM");
        }

        public boolean isComment() {
            return comment;
        }

        public boolean shouldEnd() {
            return exit;
        }

        public boolean isEndOfText() {
            return kind.equals("end-of-file");
        }

        public void reset() {
            c = '\0';
            tokenRead = "";
            pos[0] = 1;
            pos[1] = 0;
            tokenPos[0] = 1;
            tokenPos[1] = 0;
            kind = "";
            value = "";
            reader = null;
            comment = false;
            exit = false;
        }

        // Report an error
        private void reportError(char c) {
            System.out.println(position() + "\t>>>>>> Illegal Character '" + c + "'");
            exit = true;
        }
    }

    // Parsing class
    private ArrayList<Token> tokens;
    private int index;

    public Parsing(ArrayList<Token> tokens) {
        this.tokens = tokens;
        this.index = 0;
    }

    public boolean parse() {
        program();
        return true;
    }

    // Get the next token
    private Token getNextToken() {
        if (index < tokens.size()) {
            return tokens.get(index++);
        }
        return null;
    }

    // Match the expected token
    private void match(String expectedToken, String methodName) {
        Token currentToken = peekToken();
        if (currentToken != null && currentToken.getKind().equals(expectedToken)) {
            System.out.println("Expected:" + expectedToken + "from method: " + methodName);
            index++; // Move to the next token
            return;
        }
        if (expectedToken.equals(".")) {
            // If the expected token is a period, check if the current token is end-of-file
            if (currentToken != null && currentToken.getKind().equals("end-of-file")) {
                // If the current token is end-of-file, it means the program has ended unexpectedly
                error(".", methodName, currentToken);
                return;
            }
        }
        // If the expected token is not a period or the current token is not end-of-file, report a syntax error
        error(expectedToken, methodName, currentToken);
    }

    // Report a syntax error
    private void error(String expectedToken, String methodName, Token currentToken) {
        // Print detailed error message with the method name, current token information, and position
        System.out.println("Syntax error expected '" + expectedToken + "'" + "from method: " + methodName +
                " Is kind: " + (currentToken != null ? currentToken.getKind() : "null") +
                " Is value: " + (currentToken != null ? currentToken.getValue() : "null") +
                " Position: " + (currentToken != null ? currentToken.getPosition() : "null"));

        // Exit the program
        System.exit(1);
    }

    // Parse the program
    private void program() {
        String [] Follow = {"."};
        Token currentToken = peekToken();

        // Skip over any tokens until "program" keyword is found
        while (currentToken != null && !currentToken.getKind().equals("program")) {
            getNextToken(); // Move to the next token
            currentToken = peekToken(); // Update the current token
        }

        // Check if "program" keyword is found
        if (currentToken != null && currentToken.getKind().equals("program")) {
            match("program", "program"); // Match the "program" keyword
            match("ID", "program"); // Match the program identifier
            match(":", "program");
            body(new String[] {"end", "else"});
            match(".", "program");
        } else {
            error("program", "program", currentToken); // Report syntax error if "program" keyword is not found
        }
    }
    // Parse the body of the program
    private void body(String [] Follow) {
        if (peek().equals("bool") || peek().equals("int")) {
            declarations(Follow);
        }
        statements(Follow);
    }

    // Parse variable declarations
    private void declarations(String[] follow) {
        declaration(union(follow, new String[] {";"}));
        while (peek().equals("bool") || peek().equals("int")) {
            declaration(union(follow, new String[] {";"}));
        }
    }

    // Parse a single declaration
    private void declaration(String[] follow) {
        if (peek().equals("bool")) {
            match("bool", "declaration");
            match("ID", "declaration");
            while (peek().equals(",")) {
                match(",", "declaration");
                match("ID", "declaration");
            }
        } else if (peek().equals("int")) {
            match("int", "declaration");
            match("ID", "declaration");
            while (peek().equals(",")) {
                match(",", "declaration");
                match("ID", "declaration");
            }
        }
        match(";", "declaration");
    }

    // Parse statements
    private void statements(String[] follow) {
        while (peek().equals("ID") || peek().equals("if") || peek().equals("while") || peek().equals("print")) {
            statement(follow);
            if (!peek().equals(".")) {
                match(";", "statements"); // Ensure a semicolon is consumed after each statement
            }
        }
    }

    // Parse a single statement
    private void statement(String[] follow) {
        if (peek().equals("ID")) {
            match("ID", "statement");
            match(":=", "statement");
            expression(follow);
        } else if (peek().equals("if")) {
            match("if", "statement");
            expression(union(follow, new String[]{"then"}));
            match("then", "statement");
            body(new String[] {"end", "else"});
            if (peek().equals("else")) {
                match("else", "statement");
                body(new String [] {"end", "else"});
            }
            match("end", "statement");
        } else if (peek().equals("while")) {
            match("while", "statement");
            expression(union(follow, new String[]{"do"}));
            match("do", "statement");
            statements(follow);
            match("end", "statement");
        } else if (peek().equals("print")) {
            match("print", "statement");
            expression(follow);
        } else if (peek().equals(".")) {
            match(".", "statement");
        } else {
            // Error handling
            error("ID or if or while or print or .", "statement", peekToken());
        }
    }

    // Parse an expression
private void expression(String[] follow) {
    simpleExpression(follow);
    if (isRelationalOperator(peek())) {
        relationalOperator(follow); // Parse the relational operator
        simpleExpression(follow);
    }
}
    private void unaryOperator(String[] follow) {
        if (peek().equals("-") || peek().equals("not")) {
            match(peek(), "unaryOperator");
        } else {
            // Error handling
            error("UnaryOperator", "unaryOperator", peekToken());
        }
    }
    
    // Parse a relational operator
    private void relationalOperator(String[] follow) {
        if (isRelationalOperator(peek())) {
            match(peek(), "relationalOperator");
        } else {
            // Error handling
            error("Relational Operator", "relationalOperator", peekToken());
        }
    }

    // Parse a simple expression
    private void simpleExpression(String[] follow) {
        term(follow);
        while (isAdditiveOperator(peek())) {
            match(peek(), "simpleExpression");
            term(follow);
        }
    }

    // Check if the token is an additive operator
    private boolean isAdditiveOperator(String token) {
        return token.equals("+") || token.equals("-") || token.equals("or");
    }
    // Check if the token is a relational operator
    private boolean isRelationalOperator(String token) {
        return token.equals("<") || token.equals("=<") || token.equals("=") ||
               token.equals("!=") || token.equals(">=") || token.equals(">");
    }

    // Parse a term
    private void term(String[] follow) {
        factor(follow);
        while (peek().equals("*") || peek().equals("/") || peek().equals("mod") || peek().equals("and")) {
            match(peek(), "term");
            factor(follow);
        }
    }

    // Parse a factor
    private void factor(String[] follow) {
        if (peek().equals("not")|| peek().equals("-")) {
            unaryOperator(follow);
        }
        if (peek().equals("(")) {
            match("(", "factor");
            expression(union(follow, new String[]{")"}));
            match(")", "factor");
        } else if (peek().equals("ID") || peek().equals("NUM")) {
            match(peek(), "factor");
        } else if (peek().equals("mod")) {
            match("mod", "factor");
            factor(follow); // Recursively parse the factor after the modulus operator
        } else{
            // Error handling
            error("ID, NUM, (, or mod", "factor", peekToken());
        }
    }

    // Create the union of two string arrays
    private String[] union(String[] arr1, String[] arr2) {
        ArrayList<String> unionList = new ArrayList<>();
        for (String s : arr1) {
            if (!unionList.contains(s)) {
                unionList.add(s);
            }
        }
        for (String s : arr2) {
            if (!unionList.contains(s)) {
                unionList.add(s);
            }
        }
        return unionList.toArray(new String[0]);
    }

    // Peek at the next token
    private String peek() {
        Token currentToken = peekToken();
        if (currentToken != null) {
            return currentToken.getKind();
        }
        return "";
    }

    // Peek at the next token object
    private Token peekToken() {
        if (index < tokens.size()) {
            return tokens.get(index);
        }
        return null;
    }

    // Main method to execute the program
    public static void main(String[] args) {
        ArrayList<Token> tokenList = new ArrayList<>();
        boolean fileFound = false;

        do {
            // Prompt the user for the file path
            String filePath = promptForFilePath();

            try {
                // Create a FileReader object for the input file
                FileReader fileReader = new FileReader(filePath);
                // Create a LexicalAnalyzer object
                LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer();
                // Pass the FileReader object to the LexicalAnalyzer
                lexicalAnalyzer.fileInput(fileReader);

                // Loop to read each token until end of file is reached
                while (!lexicalAnalyzer.isEndOfText()) {
                    // Read the next token
                    lexicalAnalyzer.next();
                    // Create a new token object and add it to the list
                    Token token = new Token(lexicalAnalyzer.kind(),
                                             lexicalAnalyzer.stringValue(),
                                             lexicalAnalyzer.position());
                    // Add the token to the list
                    tokenList.add(token);
                }

                // Close the FileReader object
                fileReader.close();

                fileFound = true; // Set fileFound to true since the file was successfully processed

            } catch (IOException e) {
                // Handle IO exceptions
                System.out.println("Error reading file: " + e.getMessage());
            }

        } while (!fileFound); // Keep looping until a valid file is found

        // Create a Parsing object and pass the token list to it
        Parsing parsing = new Parsing(tokenList);

        // Parse the input program
        if (parsing.parse()) {
            System.out.println("Parsing successful.");
        } else {
            System.out.println("Parsing failed.");
        }
    }

    // Helper method to prompt the user for the file path
    private static String promptForFilePath() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the path to the input file: ");
        return scanner.nextLine();
    }
}