package com.company.game.Game

class Coin (var x : Float, var y : Float) {

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