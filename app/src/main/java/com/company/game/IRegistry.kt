package com.company.game

interface IRegistry {
    fun get(key: String) : String
    fun has(key: String) : Boolean
    fun put (key: String, value: String) : Boolean
}