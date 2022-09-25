import chisel3._
import chisel3.util._
class md5round extends Module{
  val io = IO(new Bundle{
    val a = Input(UInt(32.W))
    val b = Input(UInt(32.W))
    val c = Input(UInt(32.W))
    val d = Input(UInt(32.W))
    val m = Input(UInt(32.W))
    val s = Input(UInt(5.W))
    val t = Input(UInt(32.W))
    val r = Input(UInt(2.W))
    val next_a = Output(UInt(32.W))
  })
  // TODO: add code for calculating single round
  def F(x: UInt, y: UInt, z: UInt): UInt = {
    (x & y) | ((~x).asUInt & z)
  }
  def G(x: UInt, y: UInt, z: UInt): UInt = {
    (x & z) | (y & (~z).asUInt)
  }
  def H(x: UInt, y: UInt, z: UInt): UInt = {
    x ^ y ^ z
  }
  def I(x: UInt, y: UInt, z: UInt): UInt = {
    y ^ (x | (~z).asUInt)
  }

  val res = Wire(UInt(32.W)) //round return value
  res := 0.U
  switch(io.r) {
    is (0.U){
      res := io.a + F(io.b, io.c, io.d) + io.m + io.t
    }
    is (1.U) {
      res := io.a + G(io.b, io.c, io.d) + io.m + io.t
    }
    is (2.U) {
      res := io.a + H(io.b, io.c, io.d) + io.m + io.t
    }
    is (3.U) {
      res := io.a + I(io.b, io.c, io.d) + io.m + io.t
    }
  }
  val left = Wire(UInt(32.W))
  left := res << io.s
  val right = Wire(UInt(32.W))
  right := res >> (32.U - io.s)
  io.next_a := io.b + (left | right)

}
