package org.hpi.esb.tpccdatagen

import scala.util.Random

object Utils {

  private final val random: Random = Random
  random.setSeed(0)
  private final val C255 = getRandomNumber(0, 255)
  private final val C1023 = getRandomNumber(0, 1023)
  private final val C8191 = getRandomNumber(0, 8191)


  def getRandomNumber(min: Int, max: Int): Int = {
    val next: Int = random.nextInt()
    var div = next % ((max - min) + 1)
    if (div < 0) {
      div = div * -1
    }
    min + div
  }

  def getRandomAlphaNumericString(min: Int, max: Int): String = {
    var string: String = null
    val stringWithAllAllowedChars: String = "0123456789" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz"
    val allowedCharsArray = stringWithAllAllowedChars.toCharArray
    val lastArrayIndex = allowedCharsArray.length - 1
    val length = getRandomNumber(min, max)

    for (_ <- 0 until length) {
      if (string != null) {
        string = string + allowedCharsArray(getRandomNumber(0, lastArrayIndex))
      } else {
        string = "" + allowedCharsArray(getRandomNumber(0, lastArrayIndex))
      }
    }
    string
  }

  def getNURand(a: Int, x: Int, y: Int): Int = {
    val c: Int = a match {
      case 255 => C255
      case 1023 => C1023
      case 8191 => C8191
      case _ => throw new RuntimeException(s"NURand: unexpected value (a) used: $a.")
    }
    (((getRandomNumber(0, a) | getRandomNumber(x, y)) + c) % (y - x + 1)) + x
  }

  def getRandomNumberString(x: Int, y: Int): String = {
    val numbersArray = "0123456789".toCharArray
    val arrayMax = 9
    var string = "0"
    val length = getRandomNumber(x, y)

    for(_ <- 1 until length) {
        string = string + numbersArray(getRandomNumber(0, arrayMax))
    }
    string
  }
}
