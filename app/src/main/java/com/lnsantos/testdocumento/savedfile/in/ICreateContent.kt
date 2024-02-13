package com.lnsantos.testdocumento.savedfile.`in`

interface ICreateContent<I,O> {
    suspend fun create(input : I) : O
}