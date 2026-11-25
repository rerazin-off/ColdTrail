package com.example.mrdetective.data.network

import java.io.IOException

/** Обозначает запрос, отклонённый из-за отсутствия сети. */
internal class NoConnectionException : IOException()
