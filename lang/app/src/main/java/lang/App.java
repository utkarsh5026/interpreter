package lang;

import lang.lexer.Lexer;
import lang.parser.LanguageParser;
import lang.exec.evaluator.LanguageEvaluator;
import lang.exec.objects.env.Environment;
import lang.exec.base.BaseObject;
import lang.ast.statements.Program;
import lang.exec.validator.ObjectValidator;
import lang.repl.LanguageREPL;

public class App {

    public static void main(String[] args) {
        // LanguageREPL repl = new LanguageREPL();
        // repl.start();
        evaluateCode();
    }

    /**
     * Helper method to evaluate code and return the result
     */
    public static BaseObject evaluateCode() {
        String code = """
                class A {
                    method() { return "A"; }
                }

                class B extends A {
                    method() { return "B"; }
                }

                class C extends B {
                    # Inherits B's version of method
                }

                class D extends C {
                    method() { return "D"; }
                }

                let d = new D();
                d.method();
                """;

        Environment globalEnvironment = new Environment();
        LanguageEvaluator evaluator = LanguageEvaluator.withSourceCode(code, true);
        Lexer lexer = new Lexer(code);
        LanguageParser parser = new LanguageParser(lexer);
        Program program = parser.parseProgram();

        if (parser.hasErrors()) {
            parser.printErrors();
        }

        try {
            var result = evaluator.evaluateProgram(program, globalEnvironment);
            if (ObjectValidator.isError(result)) {
                var error = ObjectValidator.asError(result);
                error.printStackTrace();
            } else {
                System.out.println(result.inspect());
            }
            return null;
        } catch (Exception e) {
            System.out.println("yup");
            e.printStackTrace();
            return null;
        }
    }
}
