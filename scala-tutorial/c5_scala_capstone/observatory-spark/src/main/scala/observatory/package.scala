package object observatory {
  type Temperature = Double // °C, introduced in Week 1
  type Year = Int // Calendar year, introduced in Week 1



  val colorsAbsolute: Seq[(Temperature, Color)] = Seq(
    (60,  Color(255,  255,  255)),
    (32,  Color(255,  0,    0)),
    (12,  Color(255,  255,  0)),
    (0,   Color(0,    255,  255)),
    (-15,	Color(0,    0,    255)),
    (-27,	Color(255,  0,    255)),
    (-50,	Color(33,   0,    107)),
    (-60,	Color(0,    0,    0))
  )

  val colorsDeviation: Seq[(Temperature, Color)] = Seq(
    (7,  Color(0,   0,    0)),
    (4,  Color(255, 0,    0)),
    (2,  Color(255, 255,  0)),
    (0,  Color(255, 255,  255)),
    (-2, Color(0,   255,  255)),
    (-7, Color(0,   0,    255))
  )
}
