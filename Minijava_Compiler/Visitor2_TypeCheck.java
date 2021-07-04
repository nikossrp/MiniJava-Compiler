import syntaxtree.*;
import visitor.GJDepthFirst;
import java.lang.*;
import java.util.*;

public class Visitor2_TypeCheck extends GJDepthFirst <String, String> {
    SymbolTable st;
    int flagClass = 0;
    String curr_scope;
    String curr_class;
    String curr_meth;
    Map<String, String> temp_Store;
    int numberOfArgument;
    int indexArgument;
    Stack<Integer> numberOfArgumentList;     //keep the number of arguments for
    Stack<Integer> indexArgumentList;
    String newAllocation;  //we will use this flag to change the type of the variable in symbol table 


    public Visitor2_TypeCheck(SymbolTable s) {
        temp_Store = new HashMap<>();
        this.st = s;
        numberOfArgumentList = new Stack<>();
        indexArgumentList = new Stack<>();
        newAllocation = null;
    }

    
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

        String class_main = n.f1.accept(this, argu);
        curr_class = class_main;
        String meth_main = n.f6.tokenImage;
        curr_meth = meth_main;
        curr_scope = class_main + " " + meth_main;


        if (n.f14.present()) { //call varDecl check type for every variable
            n.f14.accept(this, argu);
        }


        if (n.f15.present()) { //call statement method if there are so.
            n.f15.accept(this, argu);
        }

//        if (!n.f15.present() && !n.f15.present())
//            throw new Exception("Error: Empty Main");


        return null;
    }



    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    //we have all classes from visitor1
    // so now we can check type for every var declaration
    public String visit(VarDeclaration n, String argu) throws Exception {
        String type = n.f0.accept(this, argu);
        if (!st.checkType(type))
            throw new Exception("invalid type " + type);

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
        curr_class = n.f1.accept(this, argu);
        if (n.f3.present())
                n.f3.accept(this, argu);

        if (n.f4.present())
            n.f4.accept(this, argu);

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
        String classNm = n.f1.accept(this, argu);
        curr_class = classNm;

        flagClass = 1;
        if (n.f5.present())
            n.f5.accept(this, argu);

        if (n.f6.present())
            n.f6.accept(this, argu);
        flagClass = 0;

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
        newAllocation = null;
        String type = n.f1.accept(this, argu);
        if (!st.checkType(type))
            throw new Exception("invalid type " + type);

        curr_meth = n.f2.accept(this, argu);

        curr_scope = curr_class + " " + curr_meth;
        if (n.f4.present())
            n.f4.accept(this, argu);
        if (n.f7.present())
            n.f7.accept(this ,argu);
        if (n.f8.present())
            n.f8.accept(this, argu);

        String return_expr = n.f10.accept(this, argu);
        String return_type = st.getVarType(curr_scope, return_expr);


        if (!return_type.equals(type)) {
            if (!st.check_for_child(type, return_type)) {
                throw new Exception("return type isn't same as method type in class " + curr_class + " in method " + curr_meth );
            }
        }

        temp_Store.clear();

        return  null;
    }



    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public String visit (AssignmentStatement n, String argu) throws  Exception {
        String var1 = n.f0.accept(this, argu);
        String op = n.f1.tokenImage;
        String var2 = n.f2.accept(this, argu);

        //return the type if allocationExpretion called before,
        // we must keep this type for acces methods from this tye and not from parents
        String temp_type = st.checkop(curr_scope, var1, op, var2);

        if (newAllocation != null || (st.getVarType(curr_scope, var1) != st.getVarType(curr_scope, var2))) {
            String newType = ((newAllocation==null) ? newAllocation : st.getVarType(curr_scope, var2));
            ClassInfo clas = st.classes.get(curr_class);
            Method met = clas.getMethod(curr_meth);
            met.changeTypeFromVar(var1, newType);
        }


        if (temp_type != null)
            temp_Store.put(var1,  temp_type);

        return null;
    }


    /**
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    public String visit(AndExpression n, String argu) throws  Exception {
        String expr1 = n.f0.accept(this, argu);
        String op = n.f1.tokenImage;
        String expr2 = n.f2.accept(this, argu);

        st.checkop(curr_scope, expr1, op, expr2);
        return "boolean";
    }


    /**
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | NotExpression()
     *       | BracketExpression()
     */
    public String visit(PrimaryExpression n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }



    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    public String visit(IfStatement n, String argu) throws Exception {
        String expr = n.f2.accept(this, argu);
        String type = st.getVarType(curr_scope, expr);

        if (!type.equals("boolean"))
            throw new Exception("Cannot convert from type " + type + " to boolean in if statement");

        n.f4.accept(this, argu);
        n.f6.accept(this, argu);

        return type;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public String visit(WhileStatement n, String argu) throws Exception {
        String expr = n.f2.accept(this, argu);
        String type = st.getVarType(curr_scope, expr);

        if (!type.equals("boolean"))
            throw new Exception("Cannot convert from type " + type + " to boolean");

        n.f4.accept(this, argu);

        return "boolean";
    }





    /**
     * f0 -> "!"
     * f1 -> Clause()
     */
    public String visit(NotExpression n, String argu) throws Exception {
        String type = n.f1.accept(this, argu);
        String expr;

        expr = st.getVarType(curr_scope, type); //if  type is a variable will return the type of the variable otherwise will keep the type

        if (expr == null) {
            throw new Exception("Undefined variable " + type);
        }

        if (!expr.equals("boolean"))
            throw new Exception("Cannot convert " + expr + " to boolean");

        return "boolean";
    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, String argu) throws Exception {
        String expr1 = n.f0.accept(this, argu);
        String op = n.f1.tokenImage;
        String expr2 = n.f2.accept(this, argu);
        st.checkop(curr_scope, expr1, op, expr2);

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, String argu) throws Exception {
        String expr1 = n.f0.accept(this, argu);
        String op = n.f1.tokenImage;
        String expr2 = n.f2.accept(this, argu);
        st.checkop(curr_scope, expr1, op, expr2);
        return "int";
    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, String argu) throws Exception {
        String expr1 = n.f0.accept(this, argu);
        String op = n.f1.tokenImage;
        String expr2 = n.f2.accept(this, argu);
        st.checkop(curr_scope, expr1, op, expr2);
        return "int";
    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, String argu) throws Exception {
        String expr1 = n.f0.accept(this, argu);
        String op = n.f1.tokenImage;
        String expr2 = n.f2.accept(this, argu);
        st.checkop(curr_scope, expr1, op, expr2);
        return "boolean";
    }

    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
    public String visit(ArrayAssignmentStatement n, String argu) throws Exception {
        String id = n.f0.accept(this, argu);
        String type = st.getVarType(curr_scope, id);

        if (type == null) {
            throw new Exception("Undefined " + id );
        }

        if (!type.equals("int[]"))
            throw new Exception("Cannot convert from type " + type + " to int[]");

        String expr1 = n.f2.accept(this, argu);
        String type1 = st.getVarType(curr_scope, expr1);

        if (!type1.equals("int"))
            throw new Exception("Cannot convert from type " + type1 + " to int");

        String expr2 = n.f5.accept(this, argu);
        String type2 = st.getVarType(curr_scope, expr2);

        if (!type2.equals("int"))
            throw new Exception("Cannot convert from type " + type2 + " to int");

        return null;
    }



    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public String visit(ArrayLookup n, String argu) throws Exception {
        String expr1 = n.f0.accept(this, argu);
        String type1 = st.getVarType(curr_scope, expr1);
        if (!type1.equals("int[]"))
            throw new Exception("Cannot convert from type " + type1 + " to type int[]");

        String expr2 = n.f2.accept(this, argu);
        String type2 = st.getVarType(curr_scope, expr2);

        if (!type2.equals("int"))
            throw new Exception("Cannot convert from type " + type2 + " to int");

        return "int";
    }



    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, String argu) throws Exception {
        String expr = n.f0.accept(this, argu);
        String type = st.getVarType(curr_scope, expr);
        if (type == null) {
            throw new Exception("undefined variable " + expr);
        }
        if (!type.equals("int[]")) {
            throw new Exception("Type " + type + " can't converted to int[]");
        }

        return "int";
    }



    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(ArrayAllocationExpression n, String argu) throws Exception {
        String expr = n.f3.accept(this, argu);
        String type = st.getVarType(curr_scope, expr);
        if (!type.equals("int"))
            throw new Exception("Expression in allocator is type " + type);

        return "int[]";
    }





    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression n, String argu) throws Exception{
        String id = n.f1.accept(this, argu);
        newAllocation = id;

        if (!st.classes.containsKey(id)) {
            throw new Exception("Undefined type " + id);
        }
        return id;
    }








    /** THIS : RETURNS THE CLASS TYPE.
     * f0 -> "this"
     */
    public String visit(ThisExpression n, String argu) throws Exception {
        return curr_class;
    }

    String current_MessageSend;
    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public String visit(MessageSend n, String argu) throws Exception {
        int flag = 0;   //flag for arguments

        String expr1 = n.f0.accept(this, argu);
        String id = n.f2.accept(this, argu);
        String typeExpr = st.getVarType(curr_scope, expr1);


        if (temp_Store.containsKey(expr1)) {
            typeExpr = temp_Store.get(expr1);
        }

        String typeID = st.checkMessageSend(typeExpr, id);

        ClassInfo clas = (ClassInfo) st.getClassInfo(typeExpr);

        Method methCheck = clas.getMethod(id);
        if (methCheck == null)
            throw new Exception("Undefined method " + id);


        String messageSend = typeExpr + "." + id;

        current_MessageSend = messageSend;

        if (n.f4.present()) {
            flag = 1;
            indexArgument = 0;
            numberOfArgument = methCheck.argumentsTypes.size();
            numberOfArgumentList.push(numberOfArgument);
            indexArgumentList.push(indexArgument);       //the i messageSend

            n.f4.accept(this, messageSend);    //send identifier at ExpressionList
        }






        if (flag == 1) {
            numberOfArgument = numberOfArgumentList.pop();
            indexArgument = indexArgumentList.pop();

            if (numberOfArgument != (indexArgument)) {
                throw new Exception ("Too few arguments in method call " + id);
            }

        }

        return typeID;
    }



    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList n, String argu) throws Exception {
        String arg = n.f0.accept(this, argu);
        String type = st.getVarType(curr_scope, arg);
        int index;



        String[] str = argu.split("\\.");
        String classCalled = str[0];
        String methodCalled = str[1];

        ClassInfo clas = (ClassInfo) st.getClassInfo(classCalled);
        Method methCall = clas.getMethod(classCalled, methodCalled);
        if (methCall == null) {
            methCall = clas.getMethod(clas.parent, methodCalled);
//          methCall.printVariables();
            if (methCall == null)
                throw new Exception("Undefined method " + methodCalled);
        }

        index = indexArgumentList.pop();


        //check first argument
        String type_temp;

        try {
            type_temp = methCall.argumentsTypes.get(index);
            index++;
            indexArgumentList.push(index);
        }
        catch(IndexOutOfBoundsException e) {
            throw new Exception("Too many arguments in function call " + methCall.name + " index = " + index);
        }

        if (!type_temp.equals(type)) {
            //check for parent type
            if (!st.check_for_child(type_temp, type)) {
                throw new Exception("Type error in method " + methCall.name);
            }
        }

        n.f1.accept(this, argu);

        return null;
    }

    /**
     * f0 -> ( ExpressionTerm() )*
     */
    public String visit(ExpressionTail n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }


    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public String visit(ExpressionTerm n, String argu) throws Exception {
        int index = indexArgumentList.pop();


        String type_temp;
        String[] str = argu.split("\\.");
        String classCalled = str[0];
        String methodCalled = str[1];

        ClassInfo clas = (ClassInfo) st.getClassInfo(classCalled);
        Method methCall = clas.getMethod(methodCalled);


        String arg = n.f1.accept(this, argu);

        String type = st.getVarType(curr_scope, arg);


        try {
            type_temp = methCall.argumentsTypes.get(index);
            index++;
        }
        catch(IndexOutOfBoundsException e) {
            throw new Exception("Too many arguments in function call " + methCall.name );
        }

        indexArgumentList.push(index);

        if (!type_temp.equals(type)) {
            if (!st.check_for_child(type_temp, type)) {
                throw new Exception("Given wrong type of argument in method called: " + methCall.name);
            }
        }

        return null;
    }



    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    public String visit(PrintStatement n, String argu) throws Exception {
        String expr = n.f2.accept(this, argu);
        String type = st.getVarType(curr_scope, expr);

        if (type != "int")
            throw new Exception("Error you should give an int type inside System.out.println(.)");

        return null;
    }



    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public String visit(BracketExpression n, String argu) throws Exception {
        n.f0.accept(this, argu);
        String _ret = n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
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

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public String visit(IntegerLiteral n, String argu) throws Exception {
        return "int";
    }


    /**
     * f0 -> "true"
     */
    public String visit(TrueLiteral n, String argu) throws Exception {
        return "boolean";
    }

    /**
     * f0 -> "false"
     */
    public String visit(FalseLiteral n, String argu) throws Exception {
        return "boolean";
    }


}
