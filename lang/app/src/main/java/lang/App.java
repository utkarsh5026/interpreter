package lang;

import lang.lexer.Lexer;
import lang.parser.LanguageParser;
import lang.exec.evaluator.LanguageEvaluator;
import lang.exec.base.BaseObject;
import lang.ast.statements.Program;
import lang.exec.objects.Environment;
import lang.repl.LanguageREPL;

public class App {

    public static void main(String[] args) {
        LanguageREPL repl = new LanguageREPL();
        repl.start();
    }

    /**
     * Helper method to evaluate code and return the result
     */
    public static BaseObject evaluateCode() {

        String code = """
                let a = 1;
                a += 1;
                a;
                """;

        Environment globalEnvironment = new Environment();
        LanguageEvaluator evaluator = new LanguageEvaluator();
        Lexer lexer = new Lexer(code);
        LanguageParser parser = new LanguageParser(lexer);
        Program program = parser.parseProgram();

        if (parser.hasErrors()) {
            parser.printErrors();
        }

        var result = evaluator.evaluateProgram(program, globalEnvironment);
        System.out.println(result.inspect());
        return result;
    }
}
