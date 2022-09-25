import chisel3._
import chisel3.util._


object defs {
  val A = 0x67452301.S(32.W).asUInt
  val B = 0xefcdab89.S(32.W).asUInt
  val C = 0x98badcfe.S(32.W).asUInt
  val D = 0x10325476.S(32.W).asUInt
  var S:Array[UInt] = Array(7.U, 12.U, 17.U, 22.U, 7.U, 12.U, 17.U, 22.U, 7.U, 12.U, 17.U, 22.U, 7.U, 12.U, 17.U, 22.U,
    5.U,  9.U, 14.U, 20.U, 5.U,  9.U, 14.U, 20.U, 5.U,  9.U, 14.U, 20.U, 5.U,  9.U, 14.U, 20.U,
    4.U, 11.U, 16.U, 23.U, 4.U, 11.U, 16.U, 23.U, 4.U, 11.U, 16.U, 23.U, 4.U, 11.U, 16.U, 23.U,
    6.U, 10.U, 15.U, 21.U, 6.U, 10.U, 15.U, 21.U, 6.U, 10.U, 15.U, 21.U, 6.U, 10.U, 15.U, 21.U)
  var K:Array[UInt] = Array(0xd76aa478L.U, 0xe8c7b756L.U, 0x242070dbL.U, 0xc1bdceeeL.U,
    0xf57c0fafL.U, 0x4787c62aL.U, 0xa8304613L.U, 0xfd469501L.U,
    0x698098d8L.U, 0x8b44f7afL.U, 0xffff5bb1L.U, 0x895cd7beL.U,
    0x6b901122L.U, 0xfd987193L.U, 0xa679438eL.U, 0x49b40821L.U,
    0xf61e2562L.U, 0xc040b340L.U, 0x265e5a51L.U, 0xe9b6c7aaL.U,
    0xd62f105dL.U, 0x02441453L.U, 0xd8a1e681L.U, 0xe7d3fbc8L.U,
    0x21e1cde6L.U, 0xc33707d6L.U, 0xf4d50d87L.U, 0x455a14edL.U,
    0xa9e3e905L.U, 0xfcefa3f8L.U, 0x676f02d9L.U, 0x8d2a4c8aL.U,
    0xfffa3942L.U, 0x8771f681L.U, 0x6d9d6122L.U, 0xfde5380cL.U,
    0xa4beea44L.U, 0x4bdecfa9L.U, 0xf6bb4b60L.U, 0xbebfbc70L.U,
    0x289b7ec6L.U, 0xeaa127faL.U, 0xd4ef3085L.U, 0x04881d05L.U,
    0xd9d4d039L.U, 0xe6db99e5L.U, 0x1fa27cf8L.U, 0xc4ac5665L.U,
    0xf4292244L.U, 0x432aff97L.U, 0xab9423a7L.U, 0xfc93a039L.U,
    0x655b59c3L.U, 0x8f0ccc92L.U, 0xffeff47dL.U, 0x85845dd1L.U,
    0x6fa87e4fL.U, 0xfe2ce6e0L.U, 0xa3014314L.U, 0x4e0811a1L.U,
    0xf7537e82L.U, 0xbd3af235L.U, 0x2ad7d2bbL.U, 0xeb86d391L.U)

}
