import java.util.*;

public class SymbolTable {

    // keep declaration class + methods/variables with scope/type
    public  Map <String, ClassInfo> classes;

    // keep extends class for every class
    public Map<String, String> inheritsFrom;

    // check declaration consistency
    public Map<String, String> methodDeclaration;
    public Map<String, String> varDeclarations;

    public Map<String, Integer> all_offsets;


    public SymbolTable() {
        classes = new LinkedHashMap<>();
        inheritsFrom = new HashMap<>();
        varDeclarations = new HashMap<>();
        methodDeclaration = new HashMap<>();
        all_offsets = new HashMap<>();
    }


    public void offsets() throws Exception {
        ArrayList<String> printed_classes = new ArrayList<>();
        ArrayList<String> grandparents = new ArrayList<>();
        String className;
        List<Integer> offset;
        int family_tree_size ;
        int methOffset = 0;
        int varOffset = 0;

        HashMap<String, List<Integer>>  offsets = new HashMap<>();  //keep offsets for every class variable/methods

        for (Object o: classes.entrySet()) {    //initialize offsets for every class
            Map.Entry entry = (Map.Entry) o;
            className = (String) entry.getKey();
            offset = new ArrayList<>(2);    //1st integer is for variable offset 2nd for method
            offset.add(varOffset);
            offset.add(methOffset);
            offsets.put(className, offset);
        }


//        System.out.println("~~~~~~~~~~~ Offsets ~~~~~~~~~~~~~~");
        // if we go on a parent class then we print only this class first after we go to children
        // taken from parent offsets
        for (Object o: classes.entrySet() ) {
            Map.Entry entry = (Map.Entry) o;
            className = (String) entry.getKey();


            if (printed_classes.contains(className)) { //if you has visit class and hasn't children
                grandparents.clear();
                continue;
            }

            get_family_members(className, grandparents, inheritsFrom);

            if (!grandparents.isEmpty()) {
                family_tree_size = grandparents.size() - 1;
                String Adam = grandparents.get(family_tree_size);

                // This practice covers the case of polymorphism
                // we get the first child which isn't printed yet
                if (printed_classes.contains(Adam)) {
                    Adam = get_firstUnvisit_parent(className, printed_classes);
                    //take offsets from Adam and keep in unvisit children
                    varOffset = offsets.get(Adam).get(0);
                    methOffset = offsets.get(Adam).get(1);
                    offsets.get(className).add(0, varOffset);
                    offsets.get(className).add(1, methOffset);
                }
                family_tree_offsets(className, offsets, printed_classes);
                grandparents.clear();
                continue;
            }

            printed_classes.add(className);
            offsets_for_class(className, offsets);

            grandparents.clear();
        }


    // --------- Insert offsets in feilds / methods - classes ---------
    // methods has the same member variables with classes (a pointer), 
    // so any change in member variable will change and the offset in variables which has the methods from class
    String[] key;
    int offs;
    ClassInfo clas;


    for (Map.Entry<String, Integer> entry: all_offsets.entrySet()) {
        key = entry.getKey().split("-");
        try {

            offs = entry.getValue();
        }
        catch(NullPointerException e) { // cover the last case :) 
            throw new Exception("Cannot extends main class");
        }
        
        if (key[0].equals("var")) {
            String clasName = key[1].split("\\.")[0];
            String varName = key[1].split("\\.")[1];
            clas = classes.get(clasName);
            clas.getVarMemberKey(varName).offset = offs;
        }
        else if (key[0].equals("meth")) {
            String clasName = key[1].split("\\.")[0];
            String methName = key[1].split("\\.")[1];
            clas = classes.get(clasName);
            clas.getMethodKey(methName).offset = offs;
        }
    }
    

        
    
    
    
    


        // clean classes from unnecessary overide methods
        for (Map.Entry<String, ClassInfo> entry: classes.entrySet()) {
            clas = entry.getValue();
            clas.remove_unnessaseryMethods();
        }

    }

    private void offsets_for_class(String className, HashMap<String, List<Integer>> offsets) {
        String scope;
        String varName;
        String methName;
        String type;
        List<Integer> curr_offset= offsets.get(className);
        int varOffset = curr_offset.get(0);
        int methOffset = curr_offset.get(1);

        ClassInfo clas = classes.get(className);

        //check if class has the main method, if does don't print
        Method met = clas.getMethod(className, "main");
        if (met != null) {
            return;
        }

    //    System.out.println("--------------Class " + clas.name + "--------------");

        // print offsets for variables
    //    System.out.println("---Variables---");
        for (Object o : clas.variables.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            scope = ((Keys) entry.getKey()).scope;
            varName = ((Keys) entry.getKey()).name;
            if (isParent_var(clas, varName)) {
                continue;
            }
            if (scope.equals(className)) {
                type = (String) entry.getValue();
                // save offsets for variables, we'll use this for v-table
                //format: var/meth - ClassName.name
                all_offsets.put("var-" + className + "." + varName, varOffset);
                // System.out.println(className + "." + varName + " : " + varOffset);
                varOffset = increase_offset(varOffset, type);
            }
        }
        curr_offset.add(0, varOffset);


        //print offsets for methods
    //    System.out.println("---Methods---");
        for (Object o : clas.methods.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            scope = ((Keys) entry.getKey()).scope;
            methName = ((Keys) entry.getKey()).name;
            if (overriding_method(clas, methName)) {
                continue;
            }
            if (scope.equals(className)) {
                // save offsets for variables, we'll use it for v-table
                all_offsets.put("meth-" + className + "." + methName, methOffset);
            //    ((Keys) entry.getKey()).Insert_offset(methOffset);        // insert offset in method
                // System.out.println(className + "." + methName + " : " + methOffset);    
                methOffset = increase_offset(methOffset, "pointer");  //size for all methods is 8 bytes.
            }
        }
        curr_offset.add(1, methOffset);

    //    System.out.println();

    }


    // check if method in clas  is overriding
    private boolean overriding_method(ClassInfo clas, String method_name) {
        ClassInfo class_temp;
        String parent;
        String name;
        ArrayList<String> grandparents = new ArrayList<>();
        get_family_members(clas.name, grandparents, inheritsFrom);

        // check if method is overwriting from a parent
        for (int i = 0; i < grandparents.size(); i++)  {
            parent = grandparents.get(i);
            class_temp = classes.get(parent);

            for (Object o_temp: class_temp.methods.entrySet() ) {
                Map.Entry entry_temp = (Map.Entry) o_temp;
                Keys key_temp = (Keys) entry_temp.getKey();
                name = key_temp.name;
                if (name.equals(method_name)) {
                    Integer parentOffset = all_offsets.get("meth-" + class_temp.name + "." + name);
                //    Method met = clas.getMethod(name);
                    all_offsets.put("meth-" + clas.name + "." + name, parentOffset);
                    // key_temp.Insert_offset(parentOffset);
                    
                    return true;
                }
            }
        }

        return false;
    }


    // check if method in clas  is overriding
    private boolean isParent_var(ClassInfo clas, String var_name) {
        ClassInfo class_temp;
        String parent;
        String name;
        ArrayList<String> grandparents = new ArrayList<>();
        get_family_members(clas.name, grandparents, inheritsFrom);

        String check = clas.getVarMemberType(var_name);
        if (check == null) {    //check in parents

                // check if method is overwriting from a parent
            for (int i = 0; i < grandparents.size(); i++)  {
                parent = grandparents.get(i);
                class_temp = classes.get(parent);

                for (Object o_temp: class_temp.variables.entrySet() ) {
                    Map.Entry entry_temp = (Map.Entry) o_temp;
                    Keys key_temp = (Keys) entry_temp.getKey();
                    name = key_temp.name;
                    if (name.equals(var_name)) {
                        
                        Integer parentOffset = all_offsets.get("var-" + class_temp.name + "." + name);
    //                    Method met = clas.getMethod(name);
                        all_offsets.put("var-" + clas.name + "." + name, parentOffset);
                        // key_temp.Insert_offset(parentOffset);

                        return true;
                    }
                }
            }
        }


        return false;
    }



    // Print offsets for parent/children classes started from parent
    public void family_tree_offsets(String firstClass, HashMap<String, List<Integer>> offsets, ArrayList<String> printed_classes)
    {
        ArrayList<String> children;
        String child;
        int varOffset, methOffset;
        children = get_children(firstClass);
        if (!printed_classes.contains(firstClass)) {
            offsets_for_class(firstClass, offsets);
            printed_classes.add(firstClass);
        }


        varOffset = offsets.get(firstClass).get(0);
        methOffset = offsets.get(firstClass).get(1);


        for (int i = 0; i < children.size(); i++) {
            child = children.get(i);

            if (printed_classes.contains(child))
                continue;


            offsets.get(child).add(0, varOffset);   //get offset from the parent
            offsets.get(child).add(1, methOffset);

            offsets_for_class(child, offsets);
            printed_classes.add(children.get(i));

            varOffset = offsets.get(child).get(0);  //keep offsets from the parent for the next child
            methOffset = offsets.get(child).get(1);

        }
    }


    private String get_firstUnvisit_parent(String child, ArrayList<String> printed_classes) {
        String parent;
        parent = child;

        while(true) {
            if (printed_classes.contains(parent))
                break;
            parent = inheritsFrom.get(child);
        }

        return parent;
    }

    public ArrayList<String> get_children(String firstClass) {
        ArrayList<String> children = new ArrayList<String>();

        Map<String, String> newHashMap = new HashMap<>();

        for(Object o : inheritsFrom.entrySet()){
            Map.Entry entry = (Map.Entry) o;
            newHashMap.put((String)entry.getValue(), (String) entry.getKey());
        }

        get_family_members(firstClass, children, newHashMap);

        return children;
    }





    public int increase_offset(int offset, String type) {
        if (type.equals("int"))  {
            offset += 4;
        }
        else if (type.equals("boolean")) {
            offset += 1;
        }
        else {  //type is a pointer
            offset += 8;
        }

        return offset;
    }


    public void Insert_class(String clas) throws Exception {
        if (classes.containsKey(clas)) {
            throw new Exception("class " + clas + " already defined");
        }
        classes.put(clas, new ClassInfo(clas));
    }


    public boolean checkType(String type)  {
        return type.equals("int") || type.equals("boolean") || type.equals("int[]") || classes.containsKey(type);
    }


    // return trthe var type
    public String getVarType(String scope, String var) {
        if (checkType(var)) //if var is the type
            return var;

        String method_scope = scope + " " + var;

        String type = varDeclarations.get(method_scope);
        if (type == null) {
            String className = scope.split(" ")[0];
            String class_scope = className ;

            ClassInfo clas = classes.get(className);
            type = clas.getVarMemberType(var);
            if (type == null) {
                /**
                 * Search identifier in parents
                 */
                ArrayList<String> grandparents = new ArrayList<>();
                get_family_members(className, grandparents, inheritsFrom);
                for (int i = 0; i < grandparents.size(); i++) {
                    String parent = grandparents.get(i);
                    String new_scope = parent + " " + var;  //member var for parent
           
                    type = varDeclarations.get(new_scope);
                    if (type != null)   //get the first variable in the way for parents (shadowing)
                        return type;
                }
            }

        }
        return type;
    }


    //get item return either classInfo object either method object
    public Object getClassInfo(String name) {
        if (classes.containsKey(name)) {
            return classes.get(name);
        }
        return null;
    }


    //this method gonna insert all variable members in the scope of  methods for every class
    public void normalizeClasses() throws Exception {
        //This is not the best way, complexity will be very hight if there are very must parentClassess
        ArrayList<String> grandparents = new ArrayList<>();
        ArrayList<String> checked_classes = new ArrayList<>();

        for (Object o: classes.entrySet() ) {
            Map.Entry entry = (Map.Entry) o;
            String child = entry.getKey().toString();

            //get only the grandparends for the child
            get_family_members(child, grandparents, inheritsFrom); // get the parents class in ArrayList

            if (!grandparents.isEmpty()) {
                for (String parent : grandparents) {
                    
                    //add parent's methods and member variables in child
                    if (!checked_classes.contains(child)) {
                        merge(child, parent);
                        checked_classes.add(child);
                    }
                }
            }
            ((ClassInfo)entry.getValue()).normalizeMethods();
            grandparents.clear();
        }
    }

    //get all grandparents/childrens for a class
    public ArrayList<String> get_family_members(String className, ArrayList<String> family_members, Map<String, String> familyTree) {
        String parent = familyTree.get(className);
        if (parent == null)
        return family_members;
        family_members.add(parent);
        return get_family_members(parent, family_members, familyTree);
    }
    
    
    public void merge(String childName, String parentName) throws Exception {
        ClassInfo child = classes.get(childName);
        ClassInfo parent = classes.get(parentName);
        String check_shadowVar;
        
        
        for (Object o: parent.variables.entrySet() ) {
            Map.Entry entry = (Map.Entry) o;
            String varName = ((Keys)entry.getKey()).name;
            String scope = ((Keys)entry.getKey()).scope;
            String type = entry.getValue().toString();
            // we need shadow fields in methos
            check_shadowVar = child.variables.get(new Keys(child.name, varName));
            // check for shadowing
            if (check_shadowVar == null) {
                child.Insert_variable((Keys)entry.getKey(), type);
            }
        }
        
        for (Object o: parent.methods.entrySet() ) {
            Map.Entry entry = (Map.Entry) o;
            String methName = ((Keys)entry.getKey()).name;
            String scope = ((Keys)entry.getKey()).scope;
            String type = ((Method)entry.getValue()).type;
            Method meth_check = child.getMethod(child.name, methName);
            Method parent_meth = parent.getMethod(methName);
            
            if (meth_check != null) {       
                //check if the method has the same format, 
                //if doesn't throw an exception
                Method met = new Method(parent_meth);
                met.stolen = true;
                int numOfArguments1 = meth_check.argumentsTypes.size();
                int numOfArguments2 = parent_meth.argumentsTypes.size();
                if (!meth_check.type.equals(type) || numOfArguments1 != numOfArguments2 || !(meth_check.argumentsTypes.equals(parent_meth.argumentsTypes))) {
                    throw new Exception("overriding method " + methName + " in class " + child.name + " not match with the base class");
                }
                //check arguments


                
                child.Insert_Method(scope, methName, met);
                continue;
            }
            child.Insert_Method(scope, methName, parent_meth);
        }
    }



    //Search for identifiers in class scope/ method scope
    public String checkop(String scope, String var1, String op, String var2) throws Exception {

        String typeVar1 = getVarType(scope, var1);
        String typeVar2 = getVarType(scope, var2);

        if ((typeVar1 == null))
            throw new Exception("undefined identifier " + var1);


        if ((typeVar2 == null))
            throw new Exception("undefined identifier " + var2);

        if (!(typeVar1.equals(typeVar2))) {

            if (op.equals("=")) {   //check for instance parent
                ArrayList<String> grandparends = new ArrayList<> ();
                grandparends = get_family_members(typeVar2, grandparends, inheritsFrom);
                if (grandparends.contains(typeVar1)) {

                    return typeVar2;
                }
            }
            throw new Exception("error: incompatible types:" +  typeVar2 + " cannot be converted to " + typeVar1);
        }


        switch (op) {
            case "+":
            case "-":
            case "*":
            case "<":
                if (!typeVar1.equals("int")) {
                    throw new Exception ("Cannot convert variable " + var1 + " from "  + typeVar1 + " to int");
                }
                if (!typeVar2.equals("int")) {
                    throw new Exception("Cannot convert variable " + var2 + " from " + typeVar2 + " to int");
                }
                break;

            case "&&":
                if (!typeVar1.equals("boolean")) {
                    throw new Exception ("Cannot convert variable " + var1 + " from " + typeVar1 + " to boolean");
                }
                if (!typeVar2.equals("boolean")) {
                    throw new Exception("Cannot convert variable " + var2 + " from " + typeVar2 + " to boolean");
                }
                break;
        }

        return null;
    }


    public void printClass() {
        for (Object o: classes.entrySet() ) {
            Map.Entry entry = (Map.Entry) o;
            System.out.println(entry.getKey());
        }
    }


    public void printInherit() {
        for (Object o: inheritsFrom.entrySet() ) {
            Map.Entry entry = (Map.Entry) o;
            System.out.println(entry.getKey() + " extends " + entry.getValue());
        }
    }

    //return the type of identifier (identifier should be in class typeExpr class)
    public String checkMessageSend(String typeExpr, String id) throws Exception {
        String type = getMethType(typeExpr, id);
        if (type == null) {
            throw new Exception("Type error not found identifier");
        }
        return type;
    }

    private String getMethType(String typeExpr, String id) throws Exception {
        String type;
        String className = typeExpr;
        ClassInfo clas = classes.get(className);
        if (clas == null) {
            throw new Exception("Type error");
        }
        type = clas.getMethodType(className, id);
        if (type == null) {
            ArrayList<String> grandparents = new ArrayList<>();
            get_family_members(className, grandparents, inheritsFrom);
            for (int i = 0; i < grandparents.size(); i++) {
                String parent = grandparents.get(i);
                String new_scope = parent + " " + id;  //member var for parent
                type = methodDeclaration.get(new_scope);    //here we could use getMethodType for every class
                if (type != null)   //get the first variable in the way for parents
                return type;
            }
        }
        
        return type;
    }

    public boolean check_for_child(String parent, String type) {
        ArrayList<String> grandparents = new ArrayList<>();
        get_family_members(type, grandparents, inheritsFrom);
        
        return grandparents.contains(parent);
    }

}

class Keys {
    public final String scope;
    public String name;
    public Integer offset;     //offset has onlye variables members, and methods all other has -1

    Keys(String key1, String key2) {
        this.scope = key1;
        this.name = key2;
        this.offset = -1;
    }

    Keys(String scope, String name, int offset) {
        this.scope = scope;
        this.name = name;
        this.offset = offset;
    }
    
    Keys(Keys k) {
        this.scope = k.scope;
        this.name = k.name;
    }
    
    public void printKey() {
        System.out.println(scope + " " + name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
           return true;
        }
        if (!(obj instanceof Keys)) {
           return false;
        }
        Keys ke = (Keys) obj;
        return scope.equals(ke.scope) && name.equals(ke.name);
     }

    @Override
    public final int hashCode() {
        int result = 17;
        if (name != null) {
            result = 31 * result + name.hashCode();
        }
        if (scope != null) {
            result = 31 * result + scope.hashCode();
        }
        return result;
    }
}




class Method {
    public String name;
    public String type;
    public Map<Keys, String> variables;
    ArrayList<String> argumentsTypes;
    boolean stolen;

    public Method(String name, String type) {
        this.name = name;
        this.type = type;
        variables = new HashMap<>();
        argumentsTypes = new ArrayList<>();
        stolen = false;
    }

    public Method(Method m) {
        this.name = m.name;
        this.type = m.type;
        variables = m.variables;
        argumentsTypes = m.argumentsTypes;
        stolen = false;
    }


    public void changeTypeFromVar(String var, String type) {
        for (Map.Entry<Keys, String> entry: variables.entrySet()) {

            if (entry.getKey().name.equals(var)) {
                entry.setValue(type);
            }
        }
    }


    
    // public String get_memberVar(String var) {
    //     String type;

    // for (Map.Entry<Keys, String> entry: variables.entrySet()) {



    // }


    //Insert variables in method (note: every method has the variables of class + arguments)
    public void Insert_Variable(String scope_name, String variable, String type) throws Exception {
        if (variables.put(new Keys(scope_name, variable), type) != null) {
            // throw  new Exception("Variable " + name + " already defined");
        }
    }


    //Insert variables in method (note: every method has the variables of class + arguments)
    public void Insert_Variable(Keys key, String type) throws Exception {
        if (variables.put(key, type) != null) {
            // throw  new Exception("Variable " + name + " already defined");
        }
    }

    public void printVariables() {
        if (variables == null) {
            return;
        }
        System.out.println("Number of variables: " + variables.size());
        for (Object o: variables.entrySet() ) {
            Map.Entry entry = (Map.Entry) o;
            String i = ((Keys)entry.getKey()).name;
            String scope = ((Keys)entry.getKey()).scope;
           int offset = ((Keys)entry.getKey()).offset;
            String type = entry.getValue().toString();
            System.out.println( "scope: " + scope + "| variable: " + i + "| type: " + type + "| offset: " + offset);
        }
    }

    public void Insert_Argument(String argu) throws Exception {
        if (argumentsTypes.add(argu) == false) {
            throw new Exception("I cannot insert argument: " + argu + " in method: " + name);
        }
    }

    public int getOffsetVar(String id) {
        int offset = -1;
        int counter = 0;

        for (Map.Entry<Keys, String> entry: variables.entrySet()) {
            if (id.equals(entry.getKey().name)) {
                counter++;
                if (offset == -1) { //get the offset in field
                    offset = entry.getKey().offset;
                }
            }
        }

        if (counter > 1) {
            return -1;
        }

        return offset;
    }

    public String getVarType(String id) {
        String name_temp;

        for (Map.Entry<Keys, String> entry: variables.entrySet()) {
            name_temp = entry.getKey().name;

            if (name_temp.equals(id)) {
                return entry.getValue();    //return the type
            }
        }

        return null;
    }


}


// // We will use that to sort methods inside class according to the offsets 
// class AccordingOffset implements Comparator<Keys> {
//     public int compare(Keys s1, Keys s2) {
//         return s1.offset.compareTo(s2.offset);
//     }
// }


class ClassInfo {
    public String name;
    public Map<String, Integer> V_Table;
    public Map<Keys, String> variables;   //variables for class only not methods
    public Map<Keys, Method> methods;
    public String parent;

    public ClassInfo(String name) {
        this.name = name;

        //We need to maintain the order of variables/methods to print offsets, so we use LinkedHashMap
        this.methods = new LinkedHashMap<>();      //methods is sorted based on offset
        this.variables = new LinkedHashMap<>();
        this.V_Table = new LinkedHashMap<>();
    }




    public void Insert_Method(String scope, String name, String type) {
        methods.put(new Keys(scope, name), new Method(name, type));
    }

    // Insert override methods
    public void Insert_Method(String scope, String name, Method parent_method) {
        methods.put(new Keys(scope, name), new Method(parent_method));
        normalizeMethods();
    }

    //insert variable in class
    public void Insert_variable(String scope, String name, String type) {
        variables.put(new Keys(scope, name), type);
    }

    public void Insert_variable(Keys key, String type) {
        variables.put(key, type);
    }

    public String getMethodType(String scope, String name) {

        for (Object o: methods.entrySet() ) {
            Map.Entry entry = (Map.Entry) o;

            String scope_temp = ((Keys)entry.getKey()).scope;
            String methName_temp = ((Keys)entry.getKey()).name;


            if (scope.equals(scope_temp) && name.equals(methName_temp))
                return ((Method)entry.getValue()).type;
        }

        return null;
    }

    //getting method based on path (this is for the child to access the parent's methods)
    public Method getMethod(String scope, String name)  {

        for (Object o: methods.entrySet() ) {
            Map.Entry entry = (Map.Entry) o;
            String scope_temp = ((Keys)entry.getKey()).scope;
            String methName_temp = ((Keys)entry.getKey()).name;

            String type_temp = ((Method)entry.getValue()).type;

            if (scope.equals(scope_temp) && name.equals(methName_temp))
                return (Method)entry.getValue();
        }

        return null;
    }


    //get method for class no methods
    public Method getMethod(String name)  {
        for (Object o: methods.entrySet() ) {
            Map.Entry entry = (Map.Entry) o;
            String i = ((Keys)entry.getKey()).name;    //key 2 is the method name
            // if (((Method)entry.getValue()).stolen)
            //     continue;

            if (i.equals(name))
                return (Method)entry.getValue();
        }

        return  null;
    }



    //insert all member variables in method
    public void normalizeMethods() {

        for (Object o: methods.entrySet() ) {
            Map.Entry entry = (Map.Entry) o;
            Method met = (Method)entry.getValue();
            for (Object o2: variables.entrySet() ) {
                try {
                    Map.Entry entry2 = (Map.Entry) o2;
                    if (met.getVarType(((Keys)entry2.getKey()).name) == null ) {  //not insert if shadowing
                        met.Insert_Variable((Keys)entry2.getKey(), entry2.getValue().toString());
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void printMethods() {
        if (methods == null)
            return;
        for (Object o: methods.entrySet() ) {
            Map.Entry entry = (Map.Entry) o;
            System.out.println( "scope: " + ((Keys)entry.getKey()).scope +
                    "| method: " + ((Keys)entry.getKey()).name +
                    "| type: " + ((Method)entry.getValue()).type
                    + "| offset: " + ((Keys)entry.getKey()).offset
                    + "| overide: " +((Method)entry.getValue()).stolen);
        }
    }

    public String getVarMemberType(String var) {

        for (Map.Entry<Keys, String> entry: variables.entrySet()) {
            String name_temp = ((Keys) entry.getKey()).name;
            if ( var.equals(name_temp))
                return entry.getValue().toString();
        }
        return null;
    }

    public Keys getVarMemberKey(String varName) {
        String name;

        for (Map.Entry<Keys, String> entry: variables.entrySet()) {
            name = entry.getKey().name;
            if (name.equals(varName)) {
                return entry.getKey();
            }
        }

        return null;
    }

    public Keys getMethodKey(String methodName) {
        String name;

        for (Map.Entry<Keys, Method> entry: methods.entrySet()) {
            name = entry.getKey().name;
            if (name.equals(methodName)) {
                return entry.getKey();
            }
        }

        return null;
    }


    public void printMemberVars() {

        for (Object o: variables.entrySet() ) {
            Map.Entry entry = (Map.Entry) o;
            String i = ((Keys)entry.getKey()).name;
            String scope = ((Keys)entry.getKey()).scope;
            int offset = ((Keys)entry.getKey()).offset;
            String type = entry.getValue().toString();
            System.out.println( "scope: " + scope + "| variable: " + i + "| type: " + type + "| offset " + offset);
        }
    }

    public int numOfMethods() {
        int numOfMethods = 0;
       for (Map.Entry<Keys, Method> entry : methods.entrySet()) {
            Method met = entry.getValue();
            if (!met.stolen) {
                numOfMethods++;
            }
        }
        return numOfMethods;
    }


    public void remove_unnessaseryMethods() {  
        int offset;
        String methName;
        for (Iterator<Map.Entry<Keys, Method>> it = methods.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Keys, Method> entry = it.next();
            
            if ("main".equals(entry.getKey().name)) {
                continue;
            }
            
            offset = entry.getKey().offset;
            if (offset == -1) {
                for (Map.Entry<Keys, Method> entry1 : methods.entrySet()) {
                    methName =  entry1.getKey().name;
                    if (methName != null) {
                        if (methName.equals(entry.getKey().name)) {
                            entry1.getValue().stolen = true;
                        }
                    }
                }
                it.remove();
            }
        }
    }




    public int getSizeOfClass() {
        int sizeOfClass = 0;
        String type;
        for (Map.Entry<Keys, String> entry: variables.entrySet()) {
            type = entry.getValue();
            sizeOfClass = increase_size(sizeOfClass, type);
        }
        return sizeOfClass;
    }

    public int increase_size(int offset, String type) {
        if (type.equals("int"))  {
            offset += 4;
        }
        else if (type.equals("boolean")) {
            offset += 1;
        }
        else {  //type is a pointer
            offset += 8;
        }

        return offset;
    }




    public int getOffsetMethod(String id) throws Exception {
        int offset = 0;
        
        for (Map.Entry<Keys, Method> entry: methods.entrySet()) {
            if (id.equals(entry.getKey().name)) {
                offset = entry.getKey().offset;
                return offset;
            }
        }
        throw new Exception("Occured an error to take offset for method: " + id + " in class: " + name);
    }
}

