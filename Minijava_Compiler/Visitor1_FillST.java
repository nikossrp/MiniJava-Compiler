import syntaxtree.*;
import visitor.GJDepthFirst;
import java.lang.*;

public class Visitor1_FillST extends GJDepthFirst <String, String> {

    public SymbolTable symbolTable = new SymbolTable();
    int flagClass = 0;
    String curr_scope;
    String className;
    String methodName;

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    public String visit (MainClass n, String argu) throws Exception {

        String mainClass = n.f1.accept(this, argu);     
        className = mainClass;

        if (symbolTable.classes.containsKey(mainClass)) {
            throw new Exception("class " + mainClass + " already defined");
        }
        symbolTable.Insert_class(mainClass);

        ClassInfo iclass = (ClassInfo)symbolTable.getClassInfo(mainClass);


        String method_type = n.f5.tokenImage;   
        String method_name = "main";   
        methodName = method_name;


        String method_scope = className;
        curr_scope = className + " " + methodName;


        if (symbolTable.methodDeclaration.containsKey(method_scope)) {
            throw new Exception("method " + method_name + " already defined");
        }
        symbolTable.methodDeclaration.put(method_scope, method_type);   //if method not declarative
        iclass.Insert_Method(method_scope, method_name, method_type);
        
        // iclass.printMethods();

        // Insert arguments in method
        String arg = n.f11.accept(this, argu);

        String argScope = mainClass + " " + method_name + " " + arg;


        if (symbolTable.varDeclarations.containsKey(argScope)) {
            throw  new Exception("argument " + arg + " already defined");
        }
        symbolTable.varDeclarations.put(argScope, "String[]"); //if variable not declarative in
        Method met = iclass.getMethod(method_name);
        met.Insert_Variable(curr_scope, arg, "String[]");

        
        if (n.f14.present())
        n.f14.accept(this, argu);
        
        return null;
    }


    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    public String visit(ClassDeclaration n, String argu) throws Exception {
        String class_name = n.f1.accept(this, argu);

        if (symbolTable.classes.containsKey(class_name)) {
            throw new Exception("class " + class_name + " already defined");
        }
        symbolTable.Insert_class(class_name);

        flagClass = 1;
        className = class_name;
        curr_scope = className;
        n.f3.accept(this, class_name);
        flagClass = 0;


        n.f4.accept(this, class_name);


        return null;
    }


    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception {
        String derived = n.f1.accept(this, argu);
        String base = n.f3.accept(this, argu);


        //check declaration of base class
        if (!symbolTable.classes.containsKey(base)) {
            throw new Exception("class " + base + " not defined");
        }
        //insert the info in inheritTable
        symbolTable.inheritsFrom.put(derived, base);

        symbolTable.Insert_class(derived);
        ClassInfo clas = symbolTable.classes.get(derived);
        clas.parent = base;


        // go to vardeclaration for derived class
        flagClass = 1;
        className = derived;
        curr_scope = className;
        if (n.f5.present())
            n.f5.accept(this, derived);
        flagClass = 0;

        //go to methDeclaration
        n.f6.accept(this, derived);



        return null;
    }




    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit (VarDeclaration n, String argu) throws Exception {
        String type = n.f0.accept(this, argu);
        String name = n.f1.accept(this, argu);
        String scope = curr_scope + " " + name;

        ClassInfo clas = (ClassInfo)symbolTable.getClassInfo(className);
        if (clas == null) {
            throw new Exception("undefined class " + className);
        }


        if (symbolTable.varDeclarations.containsKey(scope)) {
            throw new Exception("variable " + name + " already defined");
        }
        symbolTable.varDeclarations.put(scope, type);

        if (flagClass == 1) {

            clas.Insert_variable(className, name, type);
        }
        else {

            Method met = clas.getMethod(methodName);
            met.Insert_Variable(curr_scope, name, type);
        }


        return null;
    }



    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    public String visit(MethodDeclaration n, String argu) throws Exception {
        String type_method = n.f1.accept(this, argu);
        String name_method = n.f2.accept(this, argu);

        methodName = name_method;
        ClassInfo clas = (ClassInfo)symbolTable.getClassInfo(className);

        if (clas == null) {
            throw new Exception("undefined class " + argu);
        }

        curr_scope = className + " " + methodName;
        if (symbolTable.methodDeclaration.containsKey(curr_scope))
            throw new Exception("method " + methodName + " already defined");
        symbolTable.methodDeclaration.put(curr_scope, type_method);

        clas.Insert_Method(className, name_method, type_method);
        String scope = argu + " " + name_method;

        n.f4.accept(this, scope);
        n.f7.accept(this, scope);


        return null;
    }

     /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public String visit(FormalParameter n, String argu) throws Exception {

        String type = n.f0.accept(this, argu);
        String name = n.f1.accept(this, argu);

        String scope = argu + " " + name;

        ClassInfo clas = (ClassInfo) symbolTable.getClassInfo(className);
        if (clas == null) { //this will never happen but who cares
            throw new Exception("undefined class " + className);
        }
        Method met = clas.getMethod(methodName);

        if (symbolTable.varDeclarations.containsKey(scope)) {
            throw new Exception("argument " + name + " already defined");
        }
        symbolTable.varDeclarations.put(scope, type);


        met.Insert_Variable(curr_scope, name, type);

        met.Insert_Argument(type);



        return null;
    }



    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(ArrayType n, String argu) throws Exception {
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return "int[]";
    }

    /**
     * f0 -> "boolean"
     */
    public String visit(BooleanType n, String argu) throws Exception {
        return n.f0.tokenImage;
    }

    /**
     * f0 -> "int"
     */
    public String visit(IntegerType n, String argu) throws Exception {
        return n.f0.tokenImage;
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, String argu) throws Exception {
        return n.f0.tokenImage;
    }

}
