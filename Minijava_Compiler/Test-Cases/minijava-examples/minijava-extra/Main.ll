@.Main_vtable = global [0 x i8*] []

@.ArrayTest_vtable = global [1 x i8*] [
	i8* bitcast (i32 (i8*, i32)* @ArrayTest.test to i8*)
]

@.B_vtable = global [1 x i8*] [
	i8* bitcast (i32 (i8*, i32)* @B.test to i8*)
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
	%ab = alloca i8*
	%_0 = call i8* @calloc(i32 1, i32 20)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [1 x i8*], [1 x i8*]* @.ArrayTest_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1
	store i8* %_0, i8** %ab
	%_3 = load i8*, i8** %ab
	%_4 = bitcast i8* %_3 to i8***
	%_5 = load i8**, i8*** %_4
	%_6 = getelementptr i8*, i8** %_5, i32 0
	%_7 = load i8*, i8** %_6
	%_8 = bitcast i8* %_7 to i32 (i8*, i32)*
	%_9 = call i32 %_8(i8* %_3, i32 3)

	call void (i32) @print_int(i32 %_9)

	ret i32 0
}

define i32 @ArrayTest.test(i8* %this, i32 %.num) {
	%num = alloca i32
	store i32 %.num, i32* %num
	%i = alloca i32
	%intArray = alloca i32*
	%_0 = load i32, i32* %num
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

	store i32* %_4, i32** %intArray
	%_5 = getelementptr i8, i8* %this, i32 16
	%_6 = bitcast i8* %_5 to i32*
	store i32 0, i32* %_6
	%_7 = getelementptr i8, i8* %this, i32 16
	%_8 = bitcast i8* %_7 to i32*
	%_9 = load i32, i32* %_8

	call void (i32) @print_int(i32 %_9)

	%_10 = load i32*, i32** %intArray
	%_11 = load i32, i32* %_10

	call void (i32) @print_int(i32 %_11)

	store i32 0, i32* %i

	call void (i32) @print_int(i32 111)


	br label %loop_0

loop_0:
	%_12 = load i32, i32* %i
	%_13 = load i32*, i32** %intArray
	%_14 = load i32, i32* %_13
	%_15 = icmp slt i32 %_12, %_14
	br i1 %_15, label %loop_1, label %loop_2

loop_1:
	%_16 = load i32, i32* %i
	%_17 = add i32 %_16, 1

	call void (i32) @print_int(i32 %_17)

	%_18 = load i32*, i32** %intArray
	%_19 = load i32, i32* %_18
	%_20 = load i32, i32* %i
	%_21 = load i32, i32* %i
	%_22 = add i32 %_21, 1
	%_23 = icmp slt i32 %_20, %_19
	br i1 %_23, label %oob_0, label %oob_2

oob_0:
	%_24 = add i32 1, %_20
	%_25 = getelementptr i32, i32* %_18, i32 %_24
	store i32 %_22, i32* %_25
	br label %oob_2

oob_1:
	call void @throw_oob()
	br label %oob_2

oob_2:
	%_26 = load i32, i32* %i
	%_27 = add i32 %_26, 1
	store i32 %_27, i32* %i
	br label %loop_0

loop_2:

	call void (i32) @print_int(i32 222)

	store i32 0, i32* %i

	br label %loop_3

loop_3:
	%_28 = load i32, i32* %i
	%_29 = load i32*, i32** %intArray
	%_30 = load i32, i32* %_29
	%_31 = icmp slt i32 %_28, %_30
	br i1 %_31, label %loop_4, label %loop_5

loop_4:
	%_32 = load i32*, i32** %intArray
	%_33 = load i32, i32* %i
	%_34 = load i32, i32* %_32
	%_35 = icmp slt i32 %_33, %_34
	br i1 %_35, label %oob_4, label %oob_5

oob_4:
	%_36 = add i32 %_33, 1
	%_37 = getelementptr i32, i32* %_32, i32 %_36
	%_38 = load i32, i32* %_37
	br label %oob_6

oob_5:
	call void @throw_oob()
	br label %oob_6

oob_6:

	call void (i32) @print_int(i32 %_38)

	%_39 = load i32, i32* %i
	%_40 = add i32 %_39, 1
	store i32 %_40, i32* %i
	br label %loop_3

loop_5:

	call void (i32) @print_int(i32 333)

	%_41 = load i32*, i32** %intArray
	%_42 = load i32, i32* %_41
	ret i32 %_42

}

define i32 @B.test(i8* %this, i32 %.num) {
	%num = alloca i32
	store i32 %.num, i32* %num
	%i = alloca i32
	%intArray = alloca i32*
	%_0 = load i32, i32* %num
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

	store i32* %_4, i32** %intArray
	%_5 = getelementptr i8, i8* %this, i32 20
	%_6 = bitcast i8* %_5 to i32*
	store i32 12, i32* %_6
	%_7 = getelementptr i8, i8* %this, i32 20
	%_8 = bitcast i8* %_7 to i32*
	%_9 = load i32, i32* %_8

	call void (i32) @print_int(i32 %_9)

	%_10 = load i32*, i32** %intArray
	%_11 = load i32, i32* %_10

	call void (i32) @print_int(i32 %_11)

	store i32 0, i32* %i

	call void (i32) @print_int(i32 111)


	br label %loop_0

loop_0:
	%_12 = load i32, i32* %i
	%_13 = load i32*, i32** %intArray
	%_14 = load i32, i32* %_13
	%_15 = icmp slt i32 %_12, %_14
	br i1 %_15, label %loop_1, label %loop_2

loop_1:
	%_16 = load i32, i32* %i
	%_17 = add i32 %_16, 1

	call void (i32) @print_int(i32 %_17)

	%_18 = load i32*, i32** %intArray
	%_19 = load i32, i32* %_18
	%_20 = load i32, i32* %i
	%_21 = load i32, i32* %i
	%_22 = add i32 %_21, 1
	%_23 = icmp slt i32 %_20, %_19
	br i1 %_23, label %oob_0, label %oob_2

oob_0:
	%_24 = add i32 1, %_20
	%_25 = getelementptr i32, i32* %_18, i32 %_24
	store i32 %_22, i32* %_25
	br label %oob_2

oob_1:
	call void @throw_oob()
	br label %oob_2

oob_2:
	%_26 = load i32, i32* %i
	%_27 = add i32 %_26, 1
	store i32 %_27, i32* %i
	br label %loop_0

loop_2:

	call void (i32) @print_int(i32 222)

	store i32 0, i32* %i

	br label %loop_3

loop_3:
	%_28 = load i32, i32* %i
	%_29 = load i32*, i32** %intArray
	%_30 = load i32, i32* %_29
	%_31 = icmp slt i32 %_28, %_30
	br i1 %_31, label %loop_4, label %loop_5

loop_4:
	%_32 = load i32*, i32** %intArray
	%_33 = load i32, i32* %i
	%_34 = load i32, i32* %_32
	%_35 = icmp slt i32 %_33, %_34
	br i1 %_35, label %oob_4, label %oob_5

oob_4:
	%_36 = add i32 %_33, 1
	%_37 = getelementptr i32, i32* %_32, i32 %_36
	%_38 = load i32, i32* %_37
	br label %oob_6

oob_5:
	call void @throw_oob()
	br label %oob_6

oob_6:

	call void (i32) @print_int(i32 %_38)

	%_39 = load i32, i32* %i
	%_40 = add i32 %_39, 1
	store i32 %_40, i32* %i
	br label %loop_3

loop_5:

	call void (i32) @print_int(i32 333)

	%_41 = load i32*, i32** %intArray
	%_42 = load i32, i32* %_41
	ret i32 %_42

}

