# MiniJava-Compiler

<p><h3>Compile</h3></p>
make

<p><h2>Usage</h2></p>
Compile your minijava program with miniJava compiler. You will get a minijava_program_name.ll (You can compile one or more minijava files)
<br/> 
<br/>
>>java Main minijava_program_name1.java [minijava_file_name2.java] [minijava_file_name3.java]  
<br/>
<br/>
 You will need to execute the produced LLVM IR files in order to see that their output is the same as compiling the input java file with javac and executing it with java. To do that, you will need Clang with version >=4.0.0 <br/>
<br/>
>>clang -o out1 minijava_program_name.ll   
<br/>
<br/>
 Run <br/>
>>./out1



