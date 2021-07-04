import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class CreateFile_ll {
    SymbolTable st;
    FileWriter file_ll;
    String fileName;

    // Start with the first information for the .ll files
    CreateFile_ll(String fileName, SymbolTable st, Map<String, LinkedList<String>> regs) throws IOException {
        this.st = st;
        this.file_ll = new FileWriter(fileName + ".ll");
    }

    Map<String, LinkedList<String>> Create_VTables() throws IOException {
        int indexClass = 0;     //first class contain main method
        int indexMethod = 0;
        Map<String, LinkedList<String>> regs = new LinkedHashMap<>();
        Map<Keys, Method> methods = new TreeMap<>(new AccordingOffset());

        for (Map.Entry<String, ClassInfo> entryClass: st.classes.entrySet()) {    //initialize offsets for every class
            ClassInfo classInfo = entryClass.getValue();
            indexMethod = 0;

            if (indexClass == 0) {
                emit("@." + classInfo.name + "_vtable = global [0 x i8*] []");
                regs.put(classInfo.name + " main", new LinkedList<>());

                indexClass++;
                continue;
            }

            int numOfMethods;
            numOfMethods = classInfo.methods.size();
            

            emit("\n\n@." + classInfo.name + "_vtable = global ["+ numOfMethods + " x i8*] [");

            if (numOfMethods == 0) {
                emit("]");
            }

            methods.putAll(classInfo.methods);
            

            // insert methods
            for (Map.Entry<Keys, Method> entryMethod : methods.entrySet()) {
                Method meth = entryMethod.getValue();
                String scope = ((Keys)entryMethod.getKey()).scope;

                // we will need registers in llvm   (#not)
                regs.put(classInfo.name + " " + meth.name, new LinkedList<>());

                int numOfArgs = meth.argumentsTypes.size();

                //insert arguments
                emit("\n\ti8* bitcast (" + convertType(meth.type) + " (i8*");   //this*


                // +1 for 'this*'
                for (int i = 0; i < numOfArgs; i++) {
                    String argType = meth.argumentsTypes.get(i);
                    emit(", " + convertType(argType));
                }
                emit(")* @" + scope + "." + meth.name + " to i8*");

                if (indexMethod == (numOfMethods-1)) { //if it is the last method
                    emit(")\n]");
                }
                else {
                    emit("),");
                }

                indexMethod ++;

            }
            
            
            methods.clear();
        }

        emit("\n\n");

        return regs;
    }


    void Insert_info() throws IOException {

        emit("declare i8* @calloc(i32, i32)\n" +
                "declare i32 @printf(i8*, ...)\n" +
                "declare void @exit(i32)\n\n" +

                "@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n" +
                "@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n" +
            //    "@_cNSZ = constant [15 x i8] c\"Negative size\\0a\\00\"\n\n" +

                "define void @print_int(i32 %i) {\n" +
                    "\t%_str = bitcast [4 x i8]* @_cint to i8*\n" +
                    "\tcall i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n" +
                    "\tret void\n" +
                "}\n\n" +

                "define void @throw_oob() {\n" +
                        "\t%_str = bitcast [15 x i8]* @_cOOB to i8*\n" +
                        "\tcall i32 (i8*, ...) @printf(i8* %_str)\n" +
                        "\tcall void @exit(i32 1)\n" +
                        "\tret void\n" +
                "}\n\n" 
                
                /*
                + "define void @throw_nsz() {\n" + 
                    "\t%_str = bitcast [15 x i8]* @_cNSZ to i8*\n" +
                    "\tcall i32 (i8*, ...) @printf(i8* %_str)\n" + 
                    "\tcall void @exit(i32 1)\n" +
                    "\tret void\n"+
                "}\n\n"
                */
        );
    }

    void emit(String s) throws IOException {
        try {
            file_ll.write(s);
            file_ll.flush();
        }
        catch(IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    // convert type from minijava to llvm
    public String convertType(String type) {
        if(type.equals("int")){
            return "i32";
        }
        else if(type.equals("boolean")){
            return "i1";
        }
        else if(type.equals("boolean[]") || st.classes.containsKey(type) ){
            return "i8*";
        }
        else if(type.equals("int[]")){
            return "i32*";
        }

        return null;
    }

    
}

// We will use that to sort methods - emit methods for class according to the offsets 
class AccordingOffset implements Comparator<Keys> {
    @Override
    public int compare(Keys s1, Keys s2) {
        Integer offset1 = s1.offset;
        Integer offset2 = s2.offset;
        return offset1.compareTo(offset2);
    }
}
