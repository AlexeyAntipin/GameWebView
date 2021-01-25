package com.leprechaunmuch.luck.Game

import android.graphics.drawable.Drawable

class Coin (var x : Float, var y : Float, var drawable: Drawable) {

    var ttl : Int = 0

    companion object {
        var size : Int = 50
    }

    var tapped : Boolean = false

    public fun Move(_x : Float, _y : Float){
        x += _x
        y += _y
    }

    public fun GetX() : Float{
        return x;
    }

    public fun GetY() : Float{
        return y;
    }
}