@.Example1_vtable = global [0 x i8*] []

@.Test1_vtable = global [1 x i8*] [
	i8* bitcast (i32 (i8*, i32, i1)* @Test1.Start to i8*)
]

declare i8* @calloc(i32, i32)
declare i32 @printf(i8*, ...)
declare void @exit(i32)

@_cint = constant [4 x i8] c"%d\0a\00"
@_cOOB = constant [15 x i8] c"Out of bounds\0a\00"
define void @print_int(i32 %i) {
	%_str = bitcast [4 x i8]* @_cint to i8*
	call i32 (i8*, ...) @printf(i8* %_str, i32 %i)
	ret void
}

define void @throw_oob() {
	%_str = bitcast [15 x i8]* @_cOOB to i8*
	call i32 (i8*, ...) @printf(i8* %_str)
	call void @exit(i32 1)
	ret void
}


define i32 @main() {
	%_0 = call i8* @calloc(i32 1, i32 12)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [1 x i8*], [1 x i8*]* @.Test1_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1
	%_3 = bitcast i8* %_0 to i8***
	%_4 = load i8**, i8*** %_3
	%_5 = getelementptr i8*, i8** %_4, i32 0
	%_6 = load i8*, i8** %_5
	%_7 = bitcast i8* %_6 to i32 (i8*, i32, i1)*
	%_8 = call i32 %_7(i8* %_0, i32 5, i1 1)

	call void (i32) @print_int(i32 %_8)

	ret i32 0
}

define i32 @Test1.Start(i8* %this, i32 %.b, i1 %.c) {
	%b = alloca i32
	store i32 %.b, i32* %b
	%c = alloca i1
	store i1 %.c, i1* %c
	%ntb = alloca i1
	%nti = alloca i32*
	%ourint = alloca i32
	%_0 = load i32, i32* %b
	%_1 = icmp slt i32 %_0, 0
	br i1 %_1, label %arr_alloc_0, label %arr_alloc_1

arr_alloc_0:
	call void @throw_oob()
	br label %arr_alloc_1

arr_alloc_1:
	%_2 = add i32 %_0, 1
	%_3 = call i8* @calloc(i32 4, i32 %_2)
	%_4 = bitcast i8* %_3 to i32*
	store i32 %_0, i32* %_4

	store i32* %_4, i32** %nti
	%_5 = load i32*, i32** %nti
	%_6 = load i32, i32* %_5
	%_7 = icmp slt i32 0, %_6
	br i1 %_7, label %oob_0, label %oob_1

oob_0:
	%_8 = add i32 0, 1
	%_9 = getelementptr i32, i32* %_5, i32 %_8
	%_10 = load i32, i32* %_9
	br label %oob_2

oob_1:
	call void @throw_oob()
	br label %oob_2

oob_2:
	store i32 %_10, i32* %ourint
	%_11 = load i32, i32* %ourint

	call void (i32) @print_int(i32 %_11)

	%_12 = load i32*, i32** %nti
	%_13 = load i32, i32* %_12
	%_14 = icmp slt i32 0, %_13
	br i1 %_14, label %oob_3, label %oob_4

oob_3:
	%_15 = add i32 0, 1
	%_16 = getelementptr i32, i32* %_12, i32 %_15
	%_17 = load i32, i32* %_16
	br label %oob_5

oob_4:
	call void @throw_oob()
	br label %oob_5

oob_5:
	ret i32 %_17

}

