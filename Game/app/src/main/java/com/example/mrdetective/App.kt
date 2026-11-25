package com.example.mrdetective

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Точка входа приложения, инициализирующая граф зависимостей Hilt. */
@HiltAndroidApp
class App : Application()
