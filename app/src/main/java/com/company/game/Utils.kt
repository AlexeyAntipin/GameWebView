package com.company.game

import android.graphics.Paint
import java.util.*
import kotlin.math.sqrt

class Utils {

    class Math {
        companion object {
            fun dist(x1: Float, y1 : Float, x2 : Float, y2 : Float) : Float {
                return sqrt(((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)).toDouble()).toFloat()
            }

            fun rand(random : Random, from : Int, to : Int) : Int{
                return random.nextInt(to - from) + from
            }
        }
    }

    class Surface {
        companion object {
            fun getTextSizeByWidth(paint: Paint, text: String, requiredWidth : Int): Float {
                var left : Float = 0f
                var right : Float = 200f

                while(right - left > 1) {
                    val mid = (left + right) / 2
                    paint.textSize = mid * 1f

                    val width = paint.measureText(text)
                    if (width > requiredWidth) {
                        right = mid
                    } else {
                        left = mid
                    }
                }

                return right;
            }
        }
    }
}