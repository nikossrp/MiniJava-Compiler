@.OutOfBounds1_vtable = global [0 x i8*] []

@.A_vtable = global [1 x i8*] [
	i8* bitcast (i32 (i8*)* @A.run to i8*)
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
	%_0 = call i8* @calloc(i32 1, i32 8)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [1 x i8*], [1 x i8*]* @.A_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1
	%_3 = bitcast i8* %_0 to i8***
	%_4 = load i8**, i8*** %_3
	%_5 = getelementptr i8*, i8** %_4, i32 0
	%_6 = load i8*, i8** %_5
	%_7 = bitcast i8* %_6 to i32 (i8*)*
	%_8 = call i32 %_7(i8* %_0)

	call void (i32) @print_int(i32 %_8)

	ret i32 0
}

define i32 @A.run(i8* %this) {
	%a = alloca i32*
	%_0 = icmp slt i32 20, 0
	br i1 %_0, label %arr_alloc_0, label %arr_alloc_1

arr_alloc_0:
	call void @throw_oob()
	br label %arr_alloc_1

arr_alloc_1:
	%_1 = add i32 20, 1
	%_2 = call i8* @calloc(i32 4, i32 %_1)
	%_3 = bitcast i8* %_2 to i32*
	store i32 20, i32* %_3

	store i32* %_3, i32** %a
	%_4 = load i32*, i32** %a
	%_5 = load i32, i32* %_4
	%_6 = icmp slt i32 10, %_5
	br i1 %_6, label %oob_0, label %oob_1

oob_0:
	%_7 = add i32 10, 1
	%_8 = getelementptr i32, i32* %_4, i32 %_7
	%_9 = load i32, i32* %_8
	br label %oob_2

oob_1:
	call void @throw_oob()
	br label %oob_2

oob_2:

	call void (i32) @print_int(i32 %_9)

	%_10 = load i32*, i32** %a
	%_11 = load i32, i32* %_10
	%_12 = icmp slt i32 40, %_11
	br i1 %_12, label %oob_3, label %oob_4

oob_3:
	%_13 = add i32 40, 1
	%_14 = getelementptr i32, i32* %_10, i32 %_13
	%_15 = load i32, i32* %_14
	br label %oob_5

oob_4:
	call void @throw_oob()
	br label %oob_5

oob_5:
	ret i32 %_15

}

