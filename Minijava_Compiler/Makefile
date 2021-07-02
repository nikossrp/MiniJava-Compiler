all: compile

compile:
	java -jar ../jtb132di.jar -te minijava.jj
	java -jar ../javacc5.jar minijava-jtb.jj
	javac Main.java CreateFile_ll.java SymbolTable.java LLVM_Visitor.java Visitor1_FillST.java Visitor2_TypeCheck.java

clean:
	rm -f *.class *~

