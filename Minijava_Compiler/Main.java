import syntaxtree.*;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length < 1){
            System.err.println("Usage: java Main <inputFile1> <inputFile2> ..... <inputFileN>");
            System.exit(1);
        }


        FileInputStream fis = null;

        try{
            for (String s: args) {
                System.err.println("Checking program: " + s + "\n");

                fis = new FileInputStream(s);
                MiniJavaParser parser = new MiniJavaParser(fis);

                Goal root = parser.Goal();

                System.err.println("Program parsed successfully.");

                Visitor1_FillST eval = new Visitor1_FillST();
                root.accept(eval, null);

                System.out.println("Starting Semantic Analysis....");

                
                //insert all variables/methods from parent to children
                //(note: no duplicate methods, save them as a pointer to Object-Method)
                eval.symbolTable.normalizeClasses();
                
                //no need for the 3th part
                Visitor2_TypeCheck eval2 = new Visitor2_TypeCheck(eval.symbolTable);
                root.accept(eval2, null);
                
                
                System.out.println("Program is semantically correct.");
                
                eval.symbolTable.offsets();   //This is for the third part.
                
                System.out.println("Generating intermediate code (MiniJava -> LLVM)");
                
                String fileName = s.split("\\.")[0];
                LLVM_Visitor eval3 = new LLVM_Visitor(fileName, eval.symbolTable);
                root.accept(eval3, null);
                System.out.println("Generated file: " + fileName + ".ll ");
                
                System.out.println();
                
            }
        }
                catch(ParseException ex){
                    System.out.println(ex.getMessage());
                }
                catch(FileNotFoundException ex){
                    System.err.println(ex.getMessage());
                }
                finally{
                    try{
                        if(fis != null) fis.close();
                    }
                    catch(IOException ex){
                        System.err.println(ex.getMessage());
                    }
        }
    }
}