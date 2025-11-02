package com.englishmoon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class EnglishMoonApplication

fun main(args: Array<String>) {
    runApplication<EnglishMoonApplication>(*args)
}
