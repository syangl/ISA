import chisel3._
import chisel3.util._

import scala.language.postfixOps

class md5 extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(128.W))
    val in_valid = Input(Bool())

    val out = Output(UInt(128.W))
    val out_valid = Output(Bool())
    val ready = Output(Bool())
  })
  io.out := 0.U
  io.out_valid := false.B
  io.ready := false.B
  val idle :: r0 :: r1 :: r2 :: r3 :: finished :: turn_arnd :: Nil = Enum(7)
  val A = RegInit(defs.A)
  val B = RegInit(defs.B)
  val C = RegInit(defs.C)
  val D = RegInit(defs.D)
  val AA = RegInit(0.U(32.W))
  val BB = RegInit(0.U(32.W))
  val CC = RegInit(0.U(32.W))
  val DD = RegInit(0.U(32.W))
  val next_A = Wire(UInt(32.W))
  val next_B = Wire(UInt(32.W))
  val next_C = Wire(UInt(32.W))
  val next_D = Wire(UInt(32.W))
  val phase = RegInit(0.U(4.W))
  val state = RegInit(1.U(8.W))
  val next_state = Wire(UInt(8.W))
  val msg = RegInit(0.U(512.W))
  val out_r = RegInit(false.B)

  //使用的md5round模块
  val md5r = Module(new md5round)
  // inital wires
  // 笔记：个人理解为module内编程类似于是把线和硬件结构连接起来，
  // 然后按照时钟信号时序动作，最后结果和调试验证这样的编写逻辑是正确的
  next_A := A
  next_B := B
  next_C := C
  next_D := D
  next_state := state

  out_r := state(finished)
  io.out_valid := out_r
  io.ready := state(idle)
  io.out := A ## B ## C ## D
  //初始化md5round模块
  md5r.io.a := 0.U
  md5r.io.b := 0.U
  md5r.io.c := 0.U
  md5r.io.d := 0.U
  md5r.io.m := 0.U
  md5r.io.s := 0.U
  md5r.io.t := 0.U
  md5r.io.r := 0.U
  // update regs
  state := next_state
  when(next_state(idle)) {
    AA := 0.U
    BB := 0.U
    CC := 0.U
    DD := 0.U
  }.elsewhen(next_state(r0) && state(idle)) {
    AA := A
    BB := B
    CC := C
    DD := D
  }

  // TODO: add code for update A B C D
  // 仿照前两步进行ABCD和phase更新
  when(next_state(idle)) {
    A := defs.A
    B := defs.B
    C := defs.C
    D := defs.D
  }.otherwise {
    A := next_A
    B := next_B
    C := next_C
    D := next_D
  }

  // TODO: add code for update phase
  // 给定的phase只有4bits，md5roundC代码的switch逻辑是
  // i&3是周期性取0，1，2，3，对应A，D，C，B，每个循环16次，对应二进制0000~1111，
  // 因此不停给phase+1，phase可以周期性取0000~1111，对应每个round16次循环，
  // phase[1:0]周期性取0，1，2，3，对应i&3，
  // 就可以对应C代码转换成硬件模块的编程
  when(state(idle)){
    phase := 0.U
  }.otherwise{
    phase := phase + 1.U
  }


  when(next_state(idle)) {
    msg := 0.U
  }.elsewhen(next_state(r0) && state(idle)) {
    msg := Cat(io.in, io.in, io.in, io.in)
  }

  // combine logic
  // TODO: add code for the starting of the state machine
  //初始state为空闲态时，next_state为r0，其余什么都不干
  when(state(idle)){
    next_state := UIntToOH(r0)
  }
  // TODO: add code for 4 rounds calc, you must use md5round module
  val cya, cyb, cyc, cyd = Wire(UInt(32.W))
  cya := 0.U
  cyb := 0.U
  cyc := 0.U
  cyd := 0.U
  //debug
  printf(p"phase: ${phase}\n")
  // 每个时钟信号phase+1，对应C代码的循环体执行一次，
  // 因为chisel考虑一个信号执行一次，这里的编程内容相当于C代码循环体的内容
  switch(phase(1, 0)) {
    is(0.U) {
      cya := A
      cyb := B
      cyc := C
      cyd := D
    }
    is(1.U) {
      cya := D
      cyb := A
      cyc := B
      cyd := C
    }
    is(2.U) {
      cya := C
      cyb := D
      cyc := A
      cyd := B
    }
    is(3.U) {
      cya := B
      cyb := C
      cyc := D
      cyd := A
    }
  }
  //round 0
  when(state(r0)) {
    printf(p"round0\n")
    md5r.io.r := 0.U
    md5r.io.a := cya
    md5r.io.b := cyb
    md5r.io.c := cyc
    md5r.io.d := cyd
    //phase的值对应于C代码一次循环体中的i值，因为类型转换的原因最终用int i表示,
    //这里由于类型转换的不方便,找不到更好办法类型转换,
    //又要使用i作为下标（int类型）,phase取值是0~15，就用16次循环确定i的值(就是phase的值)
    //再利用i完成m，s，t的赋值
    for (i <- 0 until 16) {
      when(phase === i.U) {
        //debug
        printf(p"for phase: ${phase} i: ${i}\n")
        md5r.io.m := msg(i * 32 + 31, i * 32)//like verilog [31:0]
        md5r.io.s := defs.S(i)//defs是给出的用以define的文件，我在里面增加了S和K的定义
        md5r.io.t := defs.K(i)
      }
    }
    //对应C代码更改ABCD的部分
    switch(phase(1, 0)) {//like verilog reg[1:0]
      is(0.U) {
        next_A := md5r.io.next_a
      }
      is(1.U) {
        next_D := md5r.io.next_a
      }
      is(2.U) {
        next_C := md5r.io.next_a
      }
      is(3.U) {
        next_B := md5r.io.next_a
      }
    }
    //phase === 15 时改变next_state，而因为时序原因每个round执行16次state才会改变
    when(phase === "b1111".U){
      next_state := UIntToOH(r1)
    }
  }
  //round1,类似round0的写法
  when(state(r1)){
    printf(p"round1\n")
    md5r.io.r := 1.U
    md5r.io.a := cya
    md5r.io.b := cyb
    md5r.io.c := cyc
    md5r.io.d := cyd
    for (i <- 0 until 16) {
      when(phase === i.U) {
        val j = (i >> 1) | ((i << 3) & 0xf)
        md5r.io.m := msg(j * 32 + 31, j * 32)
        md5r.io.s := defs.S(16+i)
        md5r.io.t := defs.K(16+i)
      }
    }
    switch(phase(1, 0)) {
      is(0.U) {
        next_A := md5r.io.next_a
      }
      is(1.U) {
        next_D := md5r.io.next_a
      }
      is(2.U) {
        next_C := md5r.io.next_a
      }
      is(3.U) {
        next_B := md5r.io.next_a
      }
    }
    when(phase === "b1111".U){
      next_state := UIntToOH(r2)
    }
  }
  //round2,类似round0的写法
  when(state(r2)){
    printf(p"round2\n")
    md5r.io.r := 2.U
    md5r.io.a := cya
    md5r.io.b := cyb
    md5r.io.c := cyc
    md5r.io.d := cyd
    for (i <- 0 until 16) {
      when(phase === i.U) {
        val j = (i >> 2) | ((i << 2) & 0xf)
        md5r.io.m := msg(j * 32 + 31, j * 32)
        md5r.io.s := defs.S(32+i)
        md5r.io.t := defs.K(32+i)
      }
    }
    switch(phase(1, 0)) {
      is(0.U) {
        next_A := md5r.io.next_a
      }
      is(1.U) {
        next_D := md5r.io.next_a
      }
      is(2.U) {
        next_C := md5r.io.next_a
      }
      is(3.U) {
        next_B := md5r.io.next_a
      }
    }
    when(phase === "b1111".U){
      next_state := UIntToOH(r3)
    }
  }
  //round3,类似round0的写法
  when(state(r3)){
    printf(p"round3\n")
    md5r.io.r := 3.U
    md5r.io.a := cya
    md5r.io.b := cyb
    md5r.io.c := cyc
    md5r.io.d := cyd
    for (i <- 0 until 16) {
      when(phase === i.U) {
        val j = (i >> 3) | ((i << 1) & 0xf)
        md5r.io.m := msg(j * 32 + 31, j * 32)
        md5r.io.s := defs.S(48+i)
        md5r.io.t := defs.K(48+i)
      }
    }
    switch(phase(1, 0)) {
      is(0.U) {
        next_A := md5r.io.next_a
      }
      is(1.U) {
        next_D := md5r.io.next_a
      }
      is(2.U) {
        next_C := md5r.io.next_a
      }
      is(3.U) {
        next_B := md5r.io.next_a
      }
    }
    when(phase === "b1111".U){
      next_state := UIntToOH(finished)
    }
  }


  when(state(finished)) {
    printf(p"finished\n")
    // TODO: according to c code, add the final accumulate step
    //最后一次的next_ABCD最后输出的结果,
    //前面已经定义了输出out=A##B##C##D
    next_D := D + DD
    next_C := C + CC
    next_B := B + BB
    next_A := A + AA

    next_state := UIntToOH(turn_arnd)
  }

  when(state(turn_arnd)) {
    printf(p"turn_arnd\n")
    next_state := UIntToOH(idle)
  }
}
