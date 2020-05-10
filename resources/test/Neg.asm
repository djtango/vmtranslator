// push constant 0
@0
D=A
@SP
A=M
M=D
@SP
M=M+1
// neg
@SP
M=M-1
A=M
D=M
M=0
D=-D
@SP
A=M
M=D
@SP
M=M+1
// push constant 1
@1
D=A
@SP
A=M
M=D
@SP
M=M+1
// neg
@SP
M=M-1
A=M
D=M
M=0
D=-D
@SP
A=M
M=D
@SP
M=M+1
// push constant 8
@8
D=A
@SP
A=M
M=D
@SP
M=M+1
// neg
@SP
M=M-1
A=M
D=M
M=0
D=-D
@SP
A=M
M=D
@SP
M=M+1
// push constant 32767
@32767
D=A
@SP
A=M
M=D
@SP
M=M+1
// neg
@SP
M=M-1
A=M
D=M
M=0
D=-D
@SP
A=M
M=D
@SP
M=M+1
