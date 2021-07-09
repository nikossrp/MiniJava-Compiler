@.QuickSort_vtable = global [0 x i8*] []

@.QS_vtable = global [4 x i8*] [
	i8* bitcast (i32 (i8*, i32)* @QS.Start to i8*),
	i8* bitcast (i32 (i8*, i32, i32)* @QS.Sort to i8*),
	i8* bitcast (i32 (i8*)* @QS.Print to i8*),
	i8* bitcast (i32 (i8*, i32)* @QS.Init to i8*)
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
	%_0 = call i8* @calloc(i32 1, i32 20)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [4 x i8*], [4 x i8*]* @.QS_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1
	%_3 = bitcast i8* %_0 to i8***
	%_4 = load i8**, i8*** %_3
	%_5 = getelementptr i8*, i8** %_4, i32 0
	%_6 = load i8*, i8** %_5
	%_7 = bitcast i8* %_6 to i32 (i8*, i32)*
	%_8 = call i32 %_7(i8* %_0, i32 10)

	call void (i32) @print_int(i32 %_8)

	ret i32 0
}

define i32 @QS.Start(i8* %this, i32 %.sz) {
	%sz = alloca i32
	store i32 %.sz, i32* %sz
	%aux01 = alloca i32
	%_0 = bitcast i8* %this to i8***
	%_1 = load i8**, i8*** %_0
	%_2 = getelementptr i8*, i8** %_1, i32 3
	%_3 = load i8*, i8** %_2
	%_4 = bitcast i8* %_3 to i32 (i8*, i32)*
	%_5 = load i32, i32* %sz
	%_6 = call i32 %_4(i8* %this, i32 %_5)
	store i32 %_6, i32* %aux01
	%_7 = bitcast i8* %this to i8***
	%_8 = load i8**, i8*** %_7
	%_9 = getelementptr i8*, i8** %_8, i32 2
	%_10 = load i8*, i8** %_9
	%_11 = bitcast i8* %_10 to i32 (i8*)*
	%_12 = call i32 %_11(i8* %this)
	store i32 %_12, i32* %aux01

	call void (i32) @print_int(i32 9999)

	%_13 = getelementptr i8, i8* %this, i32 16
	%_14 = bitcast i8* %_13 to i32*
	%_15 = load i32, i32* %_14
	%_16 = sub i32 %_15, 1
	store i32 %_16, i32* %aux01
	%_17 = bitcast i8* %this to i8***
	%_18 = load i8**, i8*** %_17
	%_19 = getelementptr i8*, i8** %_18, i32 1
	%_20 = load i8*, i8** %_19
	%_21 = bitcast i8* %_20 to i32 (i8*, i32, i32)*
	%_22 = load i32, i32* %aux01
	%_23 = call i32 %_21(i8* %this, i32 0, i32 %_22)
	store i32 %_23, i32* %aux01
	%_24 = bitcast i8* %this to i8***
	%_25 = load i8**, i8*** %_24
	%_26 = getelementptr i8*, i8** %_25, i32 2
	%_27 = load i8*, i8** %_26
	%_28 = bitcast i8* %_27 to i32 (i8*)*
	%_29 = call i32 %_28(i8* %this)
	store i32 %_29, i32* %aux01
	ret i32 0

}

define i32 @QS.Sort(i8* %this, i32 %.left, i32 %.right) {
	%left = alloca i32
	store i32 %.left, i32* %left
	%right = alloca i32
	store i32 %.right, i32* %right
	%v = alloca i32
	%i = alloca i32
	%j = alloca i32
	%nt = alloca i32
	%t = alloca i32
	%cont01 = alloca i1
	%cont02 = alloca i1
	%aux03 = alloca i32
	store i32 0, i32* %t
	%_0 = load i32, i32* %left
	%_1 = load i32, i32* %right
	%_2 = icmp slt i32 %_0, %_1
	br i1 %_2, label %if_0, label %if_1

if_0:
	%_3 = getelementptr i8, i8* %this, i32 8
	%_4 = bitcast i8* %_3 to i32**
	%_5 = load i32*, i32** %_4
	%_6 = load i32, i32* %right
	%_7 = load i32, i32* %_5
	%_8 = icmp slt i32 %_6, %_7
	br i1 %_8, label %oob_0, label %oob_1

oob_0:
	%_9 = add i32 %_6, 1
	%_10 = getelementptr i32, i32* %_5, i32 %_9
	%_11 = load i32, i32* %_10
	br label %oob_2

oob_1:
	call void @throw_oob()
	br label %oob_2

oob_2:
	store i32 %_11, i32* %v
	%_12 = load i32, i32* %left
	%_13 = sub i32 %_12, 1
	store i32 %_13, i32* %i
	%_14 = load i32, i32* %right
	store i32 %_14, i32* %j
	store i1 1, i1* %cont01

	br label %loop_0

loop_0:
	%_15 = load i1, i1* %cont01
	br i1 %_15, label %loop_1, label %loop_2

loop_1:
	store i1 1, i1* %cont02

	br label %loop_3

loop_3:
	%_16 = load i1, i1* %cont02
	br i1 %_16, label %loop_4, label %loop_5

loop_4:
	%_17 = load i32, i32* %i
	%_18 = add i32 %_17, 1
	store i32 %_18, i32* %i
	%_19 = getelementptr i8, i8* %this, i32 8
	%_20 = bitcast i8* %_19 to i32**
	%_21 = load i32*, i32** %_20
	%_22 = load i32, i32* %i
	%_23 = load i32, i32* %_21
	%_24 = icmp slt i32 %_22, %_23
	br i1 %_24, label %oob_3, label %oob_4

oob_3:
	%_25 = add i32 %_22, 1
	%_26 = getelementptr i32, i32* %_21, i32 %_25
	%_27 = load i32, i32* %_26
	br label %oob_5

oob_4:
	call void @throw_oob()
	br label %oob_5

oob_5:
	store i32 %_27, i32* %aux03
	%_28 = load i32, i32* %aux03
	%_29 = load i32, i32* %v
	%_30 = icmp slt i32 %_28, %_29
	%_31 = xor i1 1, %_30
	br i1 %_31, label %if_3, label %if_4

if_3:
	store i1 0, i1* %cont02

	br label %if_5

if_4:
	store i1 1, i1* %cont02

	br label %if_5

if_5:
	br label %loop_3

loop_5:
	store i1 1, i1* %cont02

	br label %loop_6

loop_6:
	%_32 = load i1, i1* %cont02
	br i1 %_32, label %loop_7, label %loop_8

loop_7:
	%_33 = load i32, i32* %j
	%_34 = sub i32 %_33, 1
	store i32 %_34, i32* %j
	%_35 = getelementptr i8, i8* %this, i32 8
	%_36 = bitcast i8* %_35 to i32**
	%_37 = load i32*, i32** %_36
	%_38 = load i32, i32* %j
	%_39 = load i32, i32* %_37
	%_40 = icmp slt i32 %_38, %_39
	br i1 %_40, label %oob_6, label %oob_7

oob_6:
	%_41 = add i32 %_38, 1
	%_42 = getelementptr i32, i32* %_37, i32 %_41
	%_43 = load i32, i32* %_42
	br label %oob_8

oob_7:
	call void @throw_oob()
	br label %oob_8

oob_8:
	store i32 %_43, i32* %aux03
	%_44 = load i32, i32* %v
	%_45 = load i32, i32* %aux03
	%_46 = icmp slt i32 %_44, %_45
	%_47 = xor i1 1, %_46
	br i1 %_47, label %if_6, label %if_7

if_6:
	store i1 0, i1* %cont02

	br label %if_8

if_7:
	store i1 1, i1* %cont02

	br label %if_8

if_8:
	br label %loop_6

loop_8:
	%_48 = getelementptr i8, i8* %this, i32 8
	%_49 = bitcast i8* %_48 to i32**
	%_50 = load i32*, i32** %_49
	%_51 = load i32, i32* %i
	%_52 = load i32, i32* %_50
	%_53 = icmp slt i32 %_51, %_52
	br i1 %_53, label %oob_9, label %oob_10

oob_9:
	%_54 = add i32 %_51, 1
	%_55 = getelementptr i32, i32* %_50, i32 %_54
	%_56 = load i32, i32* %_55
	br label %oob_11

oob_10:
	call void @throw_oob()
	br label %oob_11

oob_11:
	store i32 %_56, i32* %t
	%_57 = getelementptr i8, i8* %this, i32 8
	%_58 = bitcast i8* %_57 to i32**
	%_59 = load i32*, i32** %_58
	%_60 = load i32, i32* %_59
	%_61 = load i32, i32* %i
	%_62 = getelementptr i8, i8* %this, i32 8
	%_63 = bitcast i8* %_62 to i32**
	%_64 = load i32*, i32** %_63
	%_65 = load i32, i32* %j
	%_66 = load i32, i32* %_64
	%_67 = icmp slt i32 %_65, %_66
	br i1 %_67, label %oob_15, label %oob_16

oob_15:
	%_68 = add i32 %_65, 1
	%_69 = getelementptr i32, i32* %_64, i32 %_68
	%_70 = load i32, i32* %_69
	br label %oob_17

oob_16:
	call void @throw_oob()
	br label %oob_17

oob_17:
	%_71 = icmp slt i32 %_61, %_60
	br i1 %_71, label %oob_12, label %oob_14

oob_12:
	%_72 = add i32 1, %_61
	%_73 = getelementptr i32, i32* %_59, i32 %_72
	store i32 %_70, i32* %_73
	br label %oob_14

oob_13:
	call void @throw_oob()
	br label %oob_14

oob_14:
	%_74 = getelementptr i8, i8* %this, i32 8
	%_75 = bitcast i8* %_74 to i32**
	%_76 = load i32*, i32** %_75
	%_77 = load i32, i32* %_76
	%_78 = load i32, i32* %j
	%_79 = load i32, i32* %t
	%_80 = icmp slt i32 %_78, %_77
	br i1 %_80, label %oob_19, label %oob_21

oob_19:
	%_81 = add i32 1, %_78
	%_82 = getelementptr i32, i32* %_76, i32 %_81
	store i32 %_79, i32* %_82
	br label %oob_21

oob_20:
	call void @throw_oob()
	br label %oob_21

oob_21:
	%_83 = load i32, i32* %j
	%_84 = load i32, i32* %i
	%_85 = add i32 %_84, 1
	%_86 = icmp slt i32 %_83, %_85
	br i1 %_86, label %if_9, label %if_10

if_9:
	store i1 0, i1* %cont01

	br label %if_11

if_10:
	store i1 1, i1* %cont01

	br label %if_11

if_11:
	br label %loop_0

loop_2:
	%_87 = getelementptr i8, i8* %this, i32 8
	%_88 = bitcast i8* %_87 to i32**
	%_89 = load i32*, i32** %_88
	%_90 = load i32, i32* %_89
	%_91 = load i32, i32* %j
	%_92 = getelementptr i8, i8* %this, i32 8
	%_93 = bitcast i8* %_92 to i32**
	%_94 = load i32*, i32** %_93
	%_95 = load i32, i32* %i
	%_96 = load i32, i32* %_94
	%_97 = icmp slt i32 %_95, %_96
	br i1 %_97, label %oob_26, label %oob_27

oob_26:
	%_98 = add i32 %_95, 1
	%_99 = getelementptr i32, i32* %_94, i32 %_98
	%_100 = load i32, i32* %_99
	br label %oob_28

oob_27:
	call void @throw_oob()
	br label %oob_28

oob_28:
	%_101 = icmp slt i32 %_91, %_90
	br i1 %_101, label %oob_23, label %oob_25

oob_23:
	%_102 = add i32 1, %_91
	%_103 = getelementptr i32, i32* %_89, i32 %_102
	store i32 %_100, i32* %_103
	br label %oob_25

oob_24:
	call void @throw_oob()
	br label %oob_25

oob_25:
	%_104 = getelementptr i8, i8* %this, i32 8
	%_105 = bitcast i8* %_104 to i32**
	%_106 = load i32*, i32** %_105
	%_107 = load i32, i32* %_106
	%_108 = load i32, i32* %i
	%_109 = getelementptr i8, i8* %this, i32 8
	%_110 = bitcast i8* %_109 to i32**
	%_111 = load i32*, i32** %_110
	%_112 = load i32, i32* %right
	%_113 = load i32, i32* %_111
	%_114 = icmp slt i32 %_112, %_113
	br i1 %_114, label %oob_33, label %oob_34

oob_33:
	%_115 = add i32 %_112, 1
	%_116 = getelementptr i32, i32* %_111, i32 %_115
	%_117 = load i32, i32* %_116
	br label %oob_35

oob_34:
	call void @throw_oob()
	br label %oob_35

oob_35:
	%_118 = icmp slt i32 %_108, %_107
	br i1 %_118, label %oob_30, label %oob_32

oob_30:
	%_119 = add i32 1, %_108
	%_120 = getelementptr i32, i32* %_106, i32 %_119
	store i32 %_117, i32* %_120
	br label %oob_32

oob_31:
	call void @throw_oob()
	br label %oob_32

oob_32:
	%_121 = getelementptr i8, i8* %this, i32 8
	%_122 = bitcast i8* %_121 to i32**
	%_123 = load i32*, i32** %_122
	%_124 = load i32, i32* %_123
	%_125 = load i32, i32* %right
	%_126 = load i32, i32* %t
	%_127 = icmp slt i32 %_125, %_124
	br i1 %_127, label %oob_37, label %oob_39

oob_37:
	%_128 = add i32 1, %_125
	%_129 = getelementptr i32, i32* %_123, i32 %_128
	store i32 %_126, i32* %_129
	br label %oob_39

oob_38:
	call void @throw_oob()
	br label %oob_39

oob_39:
	%_130 = bitcast i8* %this to i8***
	%_131 = load i8**, i8*** %_130
	%_132 = getelementptr i8*, i8** %_131, i32 1
	%_133 = load i8*, i8** %_132
	%_134 = bitcast i8* %_133 to i32 (i8*, i32, i32)*
	%_135 = load i32, i32* %left
	%_136 = load i32, i32* %i
	%_137 = sub i32 %_136, 1
	%_138 = call i32 %_134(i8* %this, i32 %_135, i32 %_137)
	store i32 %_138, i32* %nt
	%_139 = bitcast i8* %this to i8***
	%_140 = load i8**, i8*** %_139
	%_141 = getelementptr i8*, i8** %_140, i32 1
	%_142 = load i8*, i8** %_141
	%_143 = bitcast i8* %_142 to i32 (i8*, i32, i32)*
	%_144 = load i32, i32* %i
	%_145 = add i32 %_144, 1
	%_146 = load i32, i32* %right
	%_147 = call i32 %_143(i8* %this, i32 %_145, i32 %_146)
	store i32 %_147, i32* %nt

	br label %if_2

if_1:
	store i32 0, i32* %nt

	br label %if_2

if_2:
	ret i32 0

}

define i32 @QS.Print(i8* %this) {
	%j = alloca i32
	store i32 0, i32* %j

	br label %loop_0

loop_0:
	%_0 = load i32, i32* %j
	%_1 = getelementptr i8, i8* %this, i32 16
	%_2 = bitcast i8* %_1 to i32*
	%_3 = load i32, i32* %_2
	%_4 = icmp slt i32 %_0, %_3
	br i1 %_4, label %loop_1, label %loop_2

loop_1:
	%_5 = getelementptr i8, i8* %this, i32 8
	%_6 = bitcast i8* %_5 to i32**
	%_7 = load i32*, i32** %_6
	%_8 = load i32, i32* %j
	%_9 = load i32, i32* %_7
	%_10 = icmp slt i32 %_8, %_9
	br i1 %_10, label %oob_0, label %oob_1

oob_0:
	%_11 = add i32 %_8, 1
	%_12 = getelementptr i32, i32* %_7, i32 %_11
	%_13 = load i32, i32* %_12
	br label %oob_2

oob_1:
	call void @throw_oob()
	br label %oob_2

oob_2:

	call void (i32) @print_int(i32 %_13)

	%_14 = load i32, i32* %j
	%_15 = add i32 %_14, 1
	store i32 %_15, i32* %j
	br label %loop_0

loop_2:
	ret i32 0

}

define i32 @QS.Init(i8* %this, i32 %.sz) {
	%sz = alloca i32
	store i32 %.sz, i32* %sz
	%_0 = load i32, i32* %sz
	%_1 = getelementptr i8, i8* %this, i32 16
	%_2 = bitcast i8* %_1 to i32*
	store i32 %_0, i32* %_2
	%_3 = load i32, i32* %sz
	%_4 = icmp slt i32 %_3, 0
	br i1 %_4, label %arr_alloc_0, label %arr_alloc_1

arr_alloc_0:
	call void @throw_oob()
	br label %arr_alloc_1

arr_alloc_1:
	%_5 = add i32 %_3, 1
	%_6 = call i8* @calloc(i32 4, i32 %_5)
	%_7 = bitcast i8* %_6 to i32*
	store i32 %_3, i32* %_7

	%_8 = getelementptr i8, i8* %this, i32 8
	%_9 = bitcast i8* %_8 to i32**
	store i32* %_7, i32** %_9
	%_10 = getelementptr i8, i8* %this, i32 8
	%_11 = bitcast i8* %_10 to i32**
	%_12 = load i32*, i32** %_11
	%_13 = load i32, i32* %_12
	%_14 = icmp slt i32 0, %_13
	br i1 %_14, label %oob_0, label %oob_2

oob_0:
	%_15 = add i32 1, 0
	%_16 = getelementptr i32, i32* %_12, i32 %_15
	store i32 20, i32* %_16
	br label %oob_2

oob_1:
	call void @throw_oob()
	br label %oob_2

oob_2:
	%_17 = getelementptr i8, i8* %this, i32 8
	%_18 = bitcast i8* %_17 to i32**
	%_19 = load i32*, i32** %_18
	%_20 = load i32, i32* %_19
	%_21 = icmp slt i32 1, %_20
	br i1 %_21, label %oob_4, label %oob_6

oob_4:
	%_22 = add i32 1, 1
	%_23 = getelementptr i32, i32* %_19, i32 %_22
	store i32 7, i32* %_23
	br label %oob_6

oob_5:
	call void @throw_oob()
	br label %oob_6

oob_6:
	%_24 = getelementptr i8, i8* %this, i32 8
	%_25 = bitcast i8* %_24 to i32**
	%_26 = load i32*, i32** %_25
	%_27 = load i32, i32* %_26
	%_28 = icmp slt i32 2, %_27
	br i1 %_28, label %oob_8, label %oob_10

oob_8:
	%_29 = add i32 1, 2
	%_30 = getelementptr i32, i32* %_26, i32 %_29
	store i32 12, i32* %_30
	br label %oob_10

oob_9:
	call void @throw_oob()
	br label %oob_10

oob_10:
	%_31 = getelementptr i8, i8* %this, i32 8
	%_32 = bitcast i8* %_31 to i32**
	%_33 = load i32*, i32** %_32
	%_34 = load i32, i32* %_33
	%_35 = icmp slt i32 3, %_34
	br i1 %_35, label %oob_12, label %oob_14

oob_12:
	%_36 = add i32 1, 3
	%_37 = getelementptr i32, i32* %_33, i32 %_36
	store i32 18, i32* %_37
	br label %oob_14

oob_13:
	call void @throw_oob()
	br label %oob_14

oob_14:
	%_38 = getelementptr i8, i8* %this, i32 8
	%_39 = bitcast i8* %_38 to i32**
	%_40 = load i32*, i32** %_39
	%_41 = load i32, i32* %_40
	%_42 = icmp slt i32 4, %_41
	br i1 %_42, label %oob_16, label %oob_18

oob_16:
	%_43 = add i32 1, 4
	%_44 = getelementptr i32, i32* %_40, i32 %_43
	store i32 2, i32* %_44
	br label %oob_18

oob_17:
	call void @throw_oob()
	br label %oob_18

oob_18:
	%_45 = getelementptr i8, i8* %this, i32 8
	%_46 = bitcast i8* %_45 to i32**
	%_47 = load i32*, i32** %_46
	%_48 = load i32, i32* %_47
	%_49 = icmp slt i32 5, %_48
	br i1 %_49, label %oob_20, label %oob_22

oob_20:
	%_50 = add i32 1, 5
	%_51 = getelementptr i32, i32* %_47, i32 %_50
	store i32 11, i32* %_51
	br label %oob_22

oob_21:
	call void @throw_oob()
	br label %oob_22

oob_22:
	%_52 = getelementptr i8, i8* %this, i32 8
	%_53 = bitcast i8* %_52 to i32**
	%_54 = load i32*, i32** %_53
	%_55 = load i32, i32* %_54
	%_56 = icmp slt i32 6, %_55
	br i1 %_56, label %oob_24, label %oob_26

oob_24:
	%_57 = add i32 1, 6
	%_58 = getelementptr i32, i32* %_54, i32 %_57
	store i32 6, i32* %_58
	br label %oob_26

oob_25:
	call void @throw_oob()
	br label %oob_26

oob_26:
	%_59 = getelementptr i8, i8* %this, i32 8
	%_60 = bitcast i8* %_59 to i32**
	%_61 = load i32*, i32** %_60
	%_62 = load i32, i32* %_61
	%_63 = icmp slt i32 7, %_62
	br i1 %_63, label %oob_28, label %oob_30

oob_28:
	%_64 = add i32 1, 7
	%_65 = getelementptr i32, i32* %_61, i32 %_64
	store i32 9, i32* %_65
	br label %oob_30

oob_29:
	call void @throw_oob()
	br label %oob_30

oob_30:
	%_66 = getelementptr i8, i8* %this, i32 8
	%_67 = bitcast i8* %_66 to i32**
	%_68 = load i32*, i32** %_67
	%_69 = load i32, i32* %_68
	%_70 = icmp slt i32 8, %_69
	br i1 %_70, label %oob_32, label %oob_34

oob_32:
	%_71 = add i32 1, 8
	%_72 = getelementptr i32, i32* %_68, i32 %_71
	store i32 19, i32* %_72
	br label %oob_34

oob_33:
	call void @throw_oob()
	br label %oob_34

oob_34:
	%_73 = getelementptr i8, i8* %this, i32 8
	%_74 = bitcast i8* %_73 to i32**
	%_75 = load i32*, i32** %_74
	%_76 = load i32, i32* %_75
	%_77 = icmp slt i32 9, %_76
	br i1 %_77, label %oob_36, label %oob_38

oob_36:
	%_78 = add i32 1, 9
	%_79 = getelementptr i32, i32* %_75, i32 %_78
	store i32 5, i32* %_79
	br label %oob_38

oob_37:
	call void @throw_oob()
	br label %oob_38

oob_38:
	ret i32 0

}

