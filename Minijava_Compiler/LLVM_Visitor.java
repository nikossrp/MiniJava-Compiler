import syntaxtree.*;
import visitor.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;



public class LLVM_Visitor extends GJDepthFirst<String, String> {
    public SymbolTable st;
    public FileWriter file_ll;
    public String curr_scope;
    public String curr_class;
    public String curr_meth;
    public Map<String, LinkedList<String> > regs;   //first argument is the scope
    public List<String> args;
    public String saveType;     //save type to emit assignments

    // some labels for: loops, ifStatements, registers, array allocation cases
    public int register;
    public int label;
    public int if_else;
    public int nsz_ok_err;
    public int oob;
    public int arr_alloc;
    public int loop;
    public int andClause;


    LLVM_Visitor(String fileName, SymbolTable st) throws IOException {
        this.st = st;
        // Create .ll file and insert the first informations
        CreateFile_ll creator = new CreateFile_ll(fileName, st, regs);
        regs = creator.Create_VTables();
        creator.Insert_info();
        this.file_ll = creator.file_ll;
        
        args = new ArrayList<>();
        saveType = "noType";
        oob = 3;
        andClause = 0;
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
    public String visit(MainClass n, String argu) throws Exception {
        String mainClass = n.f1.accept(this, argu);
        curr_scope = mainClass + " " + "main";
        curr_class = mainClass;
        curr_meth = "main";

        emit("\ndefine i32 @main() {\n");

        if (n.f14.present()) {
            n.f14.accept(this, argu);
        }

        if (n.f15.present()) {
            n.f15.accept(this, argu);
        }

        emit("\tret i32 0\n");
        emit("}\n\n");

        return null;
    }


    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n, String argu) throws Exception {
        String type = n.f0.accept(this, argu);
        type = convertType(type);                   //get llvm type
        String id = n.f1.accept(this, argu);
        emit("\t%" + id + " = alloca " + type + "\n");

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

//        if (n.f3.present())
//            n.f3.accept(this, argu);

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
        curr_class = n.f1.accept(this, argu);

//        if (n.f5.present())
//            n.f5.accept(this, argu);

        if (n.f6.present())
            n.f6.accept(this, argu);

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
     * f8 -> ( Statement() )*       ??
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    public String visit(MethodDeclaration n, String argu) throws Exception {
        //initialize necessary labels for method
        saveType = null;
        register = 0; label = 0; 
        arr_alloc = 0; nsz_ok_err = 0;
        if_else = 0; oob = 0; 
        loop = 0;
        args.clear();


        String methType = n.f1.accept(this, argu);
        methType = convertType(methType);
        String argType;

        curr_meth = n.f2.accept(this, argu);
        curr_scope = curr_class + " " + curr_meth;

        emit("define " + methType + " @" + curr_class + "." + curr_meth + "(i8* %this");

        if (n.f4.present()) {
            n.f4.accept(this, argu);    //emit arguments
        }
        emit(") {\n");


        // Allocate space for arguments
        ClassInfo clas = st.classes.get(curr_class);
        Method meth = clas.getMethod(curr_meth);
        String arg;

        for (int i = 0; i < meth.argumentsTypes.size(); i++) {

            //get the identifier from argument
            arg = args.get(i).split("\\.")[1];

            argType = meth.argumentsTypes.get(i);
            argType = convertType(argType);

            emit("\t%" + arg + " = alloca " + argType + "\n");

            emit("\tstore " + argType + " %." + arg + ", " + argType + "* " + "%" + arg + "\n");

            LinkedList<String> reg_list = regs.get(curr_scope);
            reg_list.add("%" + arg);       
        }

        n.f7.accept(this, argu);       

        n.f8.accept(this, argu);   

        String ret = n.f10.accept(this, "Expr");
        emit("\tret " + methType + " " + ret + "\n");
        // register++;

        emit("\n}\n\n");

        return null;
    }

    
    
    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public String visit(FormalParameter n, String argu) throws Exception {
        String type = n.f0.accept(this, null);
        String id = n.f1.accept(this, null);
        type = convertType(type);
        String arg = type + " %." + id;
        args.add(arg);
        emit(", " + arg);

        return null;
    }

    
    
    
    
    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public String visit (AssignmentStatement n, String argu) throws  Exception {
        String type;
        String reg1 = n.f2.accept(this, "Expr");
        
        if (countWords(reg1) > 1) {
            reg1 = reg1.split(" ")[0];  //get the register ( came from new Id() )
        }
        
        //type is the same (we checked that in type checking) (look Visitor2)
        String reg2 = n.f0.accept(this, null);  

        // emit(";reg1: " + reg1 + "\n");
        if (reg1.equals(curr_class)) {
            reg1 = "%this";
        }
        
        if (!reg2.contains("%") && !isNumeric(reg2)) {
            reg2 = "%" + reg2;
        }
        if (!reg1.contains("%") && !isNumeric(reg1)) {
            reg1 = "%" + reg1;
        }
       
        type = convertType(saveType);
        
        emit("\tstore " + type + " " + reg1 + ", " + type + "* " + reg2 + "\n");
        
        return null;
    }




            
    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(ArrayAllocationExpression n, String argu) throws Exception {

        String length = n.f3.accept(this, "Expr");

        emit("\t%_" + register + " = icmp slt i32 " + length + ", 0\n");

        emit("\tbr i1 %_" + register + ", label %arr_alloc_" + arr_alloc + ", label %arr_alloc_" + (arr_alloc+1) + "\n\n");
        register++;

        emit("arr_alloc_" + arr_alloc + ":\n");
        arr_alloc++;
        emit("\tcall void @throw_oob()\n");
        emit("\tbr label %arr_alloc_" + arr_alloc + "\n\n");

        emit("arr_alloc_" + arr_alloc + ":\n" );
        emit("\t%_" + register + " = add i32 " + length + ", 1\n");
        register++;
        emit("\t%_" + register + " = call i8* @calloc(i32 4, i32 %_" + (register-1) + ")\n");
        register++;
        emit("\t%_" + register + " = bitcast i8* %_" + (register-1) + " to i32*\n");
        register++;
        emit("\tstore i32 " + length + ", i32* %_" + (register-1) + "\n\n");



        return "%_" + (register-1);
    }



   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
    public String visit(ArrayLength n, String argu) throws Exception{
        String reg1 = n.f0.accept(this, "Expr");

        if (!reg1.contains("%") && !isNumeric(reg1)) {
            reg1 = "%" + reg1;
        }
       
        emit("\t%_" + register + " = load i32, i32* " + reg1 + "\n");
        register++;
        return "%_" + (register-1);
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
        String expr1, expr2;
        String address, size;
        int oob1 = oob++;
        int oob2 = oob++;
        int oob3 = oob++;


        
        if (!id.contains("%")) {
            id = "%" + id;
        }
        
        emit("\t%_" + register + " = load i32*, i32** " + id + "\n");
        address = String.valueOf(register);
        register++;
        
        emit("\t%_" + register + " = load i32, i32* %_" + (register-1) + "\n");
        size = String.valueOf(register);
        register++;

        expr1 = n.f2.accept(this, "Expr");  //may load more register


        expr2 = n.f5.accept(this, "Expr"); 
        
        
        emit("\t%_" + register + " = icmp slt i32 " + expr1 + ", %_" + size + "\n");
        emit("\tbr i1 %_" + register + ", label %oob_" + oob1 + ", label %oob_" + oob3 + "\n\n");
        register++;

        emit("oob_" + oob1 + ":\n");

        emit("\t%_" + register + " = add i32 1, " + expr1 + "\n");
        register++;
        emit("\t%_" + register + " = getelementptr i32, i32* %_" + address + ", i32 %_" + (register-1) + "\n");
        register++;
        emit("\tstore i32 " + expr2 + ", i32* %_" + (register-1) + "\n");
        emit("\tbr label %oob_" + (oob3) + "\n\n");
        
        emit("oob_" + oob2 + ":\n");
        oob++;
        emit("\tcall void @throw_oob()\n");
        emit("\tbr label %oob_" + oob3 + "\n\n");
        
        emit("oob_" + oob3 + ":\n");
        
        return null; 
    }
    




    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     * 
     * e.g: int[] x;  x[0] = 1
     */
    public String visit(ArrayLookup n, String argu) throws Exception {
        saveType = "i32*";
        String address = n.f0.accept(this, "Expr");
        String size;
        String hold1 = null;
        boolean loaded = false;

        if (!address.contains("%") && !isNumeric(address)) {
            address = "%" + address;
        }
        else {
            loaded = true;
            hold1 = address;
        }
        
        if (loaded == false) {
            
            emit("\t%_" + register + " = load i32*, i32** " + address + "\n");
            hold1 = "%_" + register;
            register++;
        }
        
        String expr2 = n.f2.accept(this, "Expr");
        emit("\t%_" + register + " = load i32, i32* " + hold1   + "\n");
        size = String.valueOf(register); 
        register++;

        emit("\t%_" + register + " = icmp slt i32 " + expr2 + ", %_" + size + "\n");
        register++;

        emit("\tbr i1 %_" + (register-1) + ", label %oob_" + oob + ", label %oob_" + (oob+1) + "\n\n");
        
        emit("oob_" + oob + ":\n");
        oob++;
        emit("\t%_" + register + " = add i32 " + expr2 + ", 1\n");
        register++;
        emit("\t%_" + register + " = getelementptr i32, i32* " + hold1 + ", i32 %_" + (register-1) + "\n");
        register++;
        emit("\t%_" + register + " = load i32, i32* %_" + (register-1) + "\n");
        register++;
        emit("\tbr label %oob_" + (oob+1) + "\n\n");



        emit("oob_" + oob + ":\n");
        oob++;
        emit("\tcall void @throw_oob()\n");
        emit("\tbr label %oob_" + oob + "\n\n");
        
        emit("oob_" + oob + ":\n");
        oob++;


        return "%_" + (register-1);
    }
    
    
    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
    */
    public String visit(WhileStatement n, String argu) throws Exception {
        String expr;
        int loop1 = loop++;
        int loop2 = loop++;
        int loop3 = loop++;


        emit("\n\tbr label %loop_" + loop1 + "\n\n");
        
        emit("loop_" + loop1 + ":\n");

        expr = n.f2.accept(this, "Expr");

        emit("\tbr i1 " + expr + ", label %loop_" + loop2 + ", label %loop_" + loop3 + "\n\n");
        emit("loop_" + loop2 + ":\n");

        n.f4.accept(this, null);

        emit("\tbr label %loop_" + loop1 + "\n\n");
        emit("loop_" +loop3 + ":\n");


        return null;
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
        String expr = n.f2.accept(this, "Expr");

        int if_else1 = if_else++;
        int if_else2 = if_else++;
        int if_else3 = if_else++;


        emit("\tbr i1 " + expr + ", label %if_" + if_else1 + ", label %if_" + if_else2 + "\n\n");
        
    //if
        emit("if_" + if_else1 + ":\n");
        n.f4.accept(this, argu);
        emit("\n\tbr label %if_" + if_else3 + "\n\n");

    //else
        emit("if_" + if_else2 + ":\n");
        n.f6.accept(this, "Expr");
        emit("\n\tbr label %if_" + if_else3 + "\n\n");
        
    //then    
        emit("if_" + if_else3 + ":\n");

        
        return null;
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
        // a = b, a = 1 + 2, a = ...
        String expr = n.f0.accept(this, argu);
        return expr;
    }
    

    /**
     * f0 -> AndExpression()
     *       | CompareExpression()
     *       | PlusExpression()
     *       | MinusExpression()
     *       | TimesExpression()
     *       | ArrayLookup()
     *       | ArrayLength()
     *       | MessageSend()
     *       | PrimaryExpression()
    */
   public String visit(Expression n, String argu) throws Exception {
        String expr = n.f0.accept(this, argu);
        return expr;
   }
    

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
    */
    public String visit(BracketExpression n, String argu) throws Exception {
        String expr = n.f1.accept(this, argu); 
        return expr;
    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, String argu) throws Exception {
        saveType = "i32";
        String expr1 = n.f0.accept(this, "Expr");
        String expr2 = n.f2. accept(this, "Expr");

        emit("\t%_" + register + " = add i32 " + expr1 + ", " + expr2 + "\n");
        register++;

        return "%_" + (register-1);
    }   



    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
    */
   public String visit(TimesExpression n, String argu) throws Exception {
    saveType = "i32";
    String expr1 = n.f0.accept(this, "Expr");
    String expr2 = n.f2.accept(this, "Expr");
    
    emit("\t%_" + register + " = mul i32 " + expr1 + ", " + expr2 + "\n");
    register++;

    return "%_" + (register-1);
   }



    /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
    public String visit(MinusExpression n, String argu) throws Exception {
        saveType = "i32";
        String expr1 = n.f0.accept(this, "Expr");
        String expr2 = n.f2.accept(this, "Expr");
        
        emit("\t%_" + register + " = sub i32 " + expr1 + ", " + expr2 + "\n");
        register++;

        return "%_" + (register-1);
    }



    /**
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    public String visit(AndExpression n, String argu) throws Exception {
        saveType = "i1";

        //when identifier get the message "expr" he load the values.
        String expr1, expr2;

        expr1 = n.f0.accept(this, "Expr"); 

        int andClause1 = andClause++;
        int andClause2 = andClause++;
        int andClause3 = andClause++;
        int andClause4 = andClause++;


        emit("\tbr label %andClause_" + andClause1 + "\n\n");
        emit("andClause_" + andClause1 + ":\n");
        emit("\tbr i1 " + expr1 + ", label %andClause_" + andClause2 + ", label %andClause_" + andClause4 + "\n\n"); 
        
        emit("andClause_" + andClause2 + ":\n");
        expr2 = n.f2.accept(this, "Expr");

        emit("\tbr label %andClause_" + andClause3 + "\n\n");
        
        emit("andClause_" + andClause3 + ":\n");
        emit("\tbr label %andClause_" + andClause4 + "\n\n");

        emit("andClause_" + andClause4 + ":\n");
        emit("\t%_" + register + " = phi i1 [0, %andClause_" + andClause1 + "], [" + expr2 + ", %andClause_" + andClause3 + "]\n");
        register++;

        return "%_" + (register-1);
    }
    


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
    */
    public String visit(CompareExpression n, String argu) throws Exception {
        saveType = "i1";
        String expr1 = n.f0.accept(this, "Expr");
        String expr2 = n.f2.accept(this, "Expr");
        emit("\t%_" + register + " = icmp slt i32 " + expr1 + ", " + expr2 + "\n");
        register++;

        return "%_" + (register-1);
    }


    /**
    * f0 -> "!"
    * f1 -> Clause()
    */
    public String visit(NotExpression n, String argu) throws Exception {
        saveType = "i1";
        String regClause = n.f1.accept(this, "Expr");
        
        emit("\t%_" + register + " = xor i1 1, " + regClause + "\n");
        register++;

        return "%_" + (register-1);
    }




    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
    */
    public String visit(AllocationExpression n, String argu) throws Exception {

        //with null we get only the identifier
        String id = n.f1.accept(this, null);
        ClassInfo clas = st.classes.get(id);
        int size = clas.getSizeOfClass() + 8;   // +8 for vtable   
        int vTableSize = clas.methods.size();
        /*
         * The first argument is the amount of objects we want to allocate
           (always 1 for object allocation, but this is handy when we will look at arrays)
         * The second argument is the size of the object. This is calculated as the sum of the
           size of the fields of the class and all the super classes PLUS 8 bytes, to account for
           the vtable pointer.
        */
        emit("\t%_" + register + " = call i8* @calloc(i32 1, i32 " + size +")\n");
        register++;

        emit("\t%_" + register + " = bitcast i8* %_" + (register-1) + " to i8***\n");
        register++;

        // Get the address of the first element of the Base_vtable
        emit("\t%_"+register+" = getelementptr ["+vTableSize+" x i8*], ["+vTableSize+" x i8*]* @." + id + "_vtable, i32 0, i32 0\n");
        emit("\tstore i8** %_" + (register) + ", i8*** %_" + (register-1) + "\n");
        register++;


        return "%_" + (register-3) + " " + id;
    }


    
    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    String PrimaryType;
    int index;
    int numOfArgs;
    boolean messageSend;
    public String visit(MessageSend n, String argu) throws Exception {
        messageSend = true;
        // emit("\n\t;------ MessageSend called ----\n");
        int offset = 0;
        
        String check = n.f0.accept(this, "MessageSend");
        String id = n.f2.accept(this, null);
        
        String[] s = null;
        String primId = saveType, reg = null;
        
        if (countWords(check) > 1) {
            s = check.split(" ");
            reg = s[0];         //identifier (maybe it is the "%this")
            primId = s[1];  //primaryExpr  
        }
        else if (!check.contains("%")) {
            reg = "%this";
            primId = check; 
            //we have the name of the class 
        }
        else if (check.contains("%")) {
            reg = check;
        }
        
        String type;
        
        if (!st.classes.containsKey(primId)) {
            ClassInfo clas = st.classes.get(curr_class);
            // clas.printMethods();
            Method meth = clas.getMethod(curr_meth);
            type = meth.getVarType(primId);
        }
        else {
            type = primId;
        }
        if ((type == null) || type.contains("%")) {   //came from messageSend
            type = saveType;
        }
        
        PrimaryType = type;
        
        ClassInfo clas = st.classes.get(type);
        Method met = clas.getMethod(id);
        String methType = convertType(met.type);
        saveType = met.type;
        offset = clas.getOffsetMethod(id);
        String args = "";
        String hold1;
        
        
        emit("\t%_" + register + " = bitcast i8* " + reg + " to i8***\n");
        register++;
        
        emit("\t%_" + register + " = load i8**, i8*** %_" + (register-1) + "\n");
        register++;
        
        // offset/8 ( because GEP can translates offset itself)  
        emit("\t%_" + register + " = getelementptr i8*, i8** %_" + (register-1) + ", i32 " + offset/8 + "\n");  
        register++;
        
        emit("\t%_" + register + " = load i8*, i8** %_" + (register-1) + "\n");
        register++;
        
        index = 0;
        emit("\t%_" + register + " = bitcast i8* %_" + (register-1) + " to " + methType);
        hold1 = "%_" + register;
        emit(" (i8*");
        
        
        
        
        // insert arguments / no need to use other
        // visitor for arguments we have them in symbol table 
        numOfArgs = met.argumentsTypes.size();
        for (int i = 0; i < numOfArgs; i++) {
            String argType = met.argumentsTypes.get(i);
            emit(", " + convertType(argType));
        }
        emit(")*\n");
        
        register++;
        
        if (n.f4.present()) {   
            //load arguments
            args = n.f4.accept(this, id);   //for method id
        }
        

        emit("\t%_" + register + " = call " + methType + " " + hold1 + "(i8* " + reg + args + ")\n");
        register++;

        messageSend = false;

        return "%_" + (register-1);
    }


    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList n, String argu) throws Exception {
        String arg = n.f0.accept(this, "Expr");
        String allArgs = ", ";
        String meth = argu;     //get method (messageSend sent that)
        String type;

        if  (countWords(arg) > 1) {
            arg = arg.split(" ")[0];    //get the register  (we cant get type from st)
        }
        if (arg.equals(curr_class)) {
            arg = "%this";
        }
        
        ClassInfo clas = st.classes.get(PrimaryType);
        Method met = clas.getMethod(meth);
        type = convertType(met.argumentsTypes.get(index++));    
        
        // emit(";AllArgs: " + allArgs + "\n");
        allArgs += type + " " + arg; 

        this.args.clear();
        n.f1.accept(this, argu);

        //apply others arguments
        for (int i = 0; i < this.args.size(); i++) {
            allArgs += ", " + this.args.get(i);
        }
        
        
        return allArgs;
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
        String arg = n.f1.accept(this, "Expr");
        String meth = argu, type;

        ClassInfo clas = st.classes.get(PrimaryType);
        Method met = clas.getMethod(meth);
        type = convertType(met.argumentsTypes.get(index++));

        this.args.add(type + " " + arg);
        return  null;
    }



    /** THIS : RETURNS THE CLASS TYPE.
     * f0 -> "this"
     */
    public String visit(ThisExpression n, String argu) throws Exception {
        return curr_class;
    }




    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */  
    public String visit(PrintStatement n, String argu) throws Exception {
        String expr = n.f2.accept(this, "Expr");
        emit("\n\tcall void (i32) @print_int(i32 " + expr +")\n\n");
        return null;
    }
    
    
    
    


    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, String argu) throws Exception {
        String id = n.f0.tokenImage;
        String type, java_type;
        

        if ((st.classes.get(id) != null) ) {   //if identifier is a className return id
            saveType = "i8*";   //need this for assignment 
            return id;
        }
        
        int offset = -1;
        ClassInfo clas = st.classes.get(curr_class);
        Method meth = clas.getMethod(curr_meth);
        if (meth != null) {
            //if we want to access a field of the class
            offset = meth.getOffsetVar(id);
            type = meth.getVarType(id);
            
            if (type != null) {
                saveType = type;
                type = convertType(type);
            }

            if (offset != -1) {


                emit("\t%_" + register + " = getelementptr i8, i8* %this, i32 " + (offset+8) + "\n");  
                register++;
                
                emit("\t%_" + register + " = bitcast i8* %_" + (register-1) + " to " + type + "*\n");
                register++;
                
                if (argu != null) {
                    if (argu.equals("Expr") || (messageSend == true)) {
                        emit("\t%_" + register + " = load " + type + ", " + type + "* %_" + (register-1) + "\n");
                        register++;
                    }
                }


                
                return "%_" + (register-1);
            }
        }
        
        if (argu != null) {
            if (argu.equals("Expr") || argu.equals("MessageSend")) {
                type = st.getVarType(curr_scope, id);
                saveType = type;
                type = convertType(type);
            
                emit("\t%_" + register + " = load " + type + ", " + type + "* %" + id + "\n");
                register++;
                
                if (argu.equals("Expr")) {
                    return "%_" + (register-1);
                }
                else if (argu.equals("MessageSend")) {
                    //we will use id to find the type of variable in messageSend and then the method
                    return "%_" + (register -1) + " " + id; 
                }
                
            }
        }
        
        return id;
        
    }
        
        
        
    public String get_nextFree_reg() {
        String next_reg = null;
        LinkedList<String> regs_list = regs.get(curr_scope);
        String last_reg = regs_list.getLast();
        String last_number = last_reg.substring(last_reg.length()-1);
        Integer last_number_int = Integer.parseInt(last_number) + 1;
        next_reg = "%_" + last_number_int.toString();
        return next_reg;
    }
    
    






    // convert type from minijava to llvm
    public String convertType(String type) throws Exception {
        
        List<String> llvm_types = new ArrayList<String>() {{
            add("i8");
            add("i8*");
            add("i8**");
            add("i8***");
            add("i1");
            add("i32");
            add("i32*");
        }};

        if (llvm_types.contains(type)) {
            return type;
        }

        if(type.equals("int") || isNumeric(type)){
            return "i32";
        }
        else if(type.equals("boolean") || type.equals("true") || type.equals("false")){
            return "i1";
        }
        else if(st.classes.containsKey(type) || type.equals("%this")){
            return "i8*";      
        }
        else if(type.equals("int[]") ){
            return "i32*";
        }

        throw new Exception("An error occurred at the convertType method, cannot convert type: " + type);
    }


    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            int d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static int countWords(String input) { 
        if (input == null || input.isEmpty()) { 
            return 0; 
        } 
        
        String[] words = input.split("\\s+"); 
        return words.length; 
    }



    void emit(String s) {
        try {
            file_ll.write(s);
            file_ll.flush();
        }
        catch(IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public String visit(IntegerLiteral n, String argu) throws Exception {
        return n.f0.tokenImage;
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
     * f0 -> "true"
     */
    public String visit(TrueLiteral n, String argu) throws Exception {
        return "1";
    }

    /**
     * f0 -> "false"
     */
    public String visit(FalseLiteral n, String argu) throws Exception {
        return "0";
    }

}
