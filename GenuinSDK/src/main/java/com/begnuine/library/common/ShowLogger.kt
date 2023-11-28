package com.begenuine.feedscreensdk.common

import java.util.logging.Level
import java.util.logging.Logger

class ShowLogger {
    var logger = Logger.getLogger(ShowLogger::class.java.name)

    /*
    * Method to log the exception and we can do any operation on exception object.
    *
    * @params tag is the string value which show something about exception.
    * @params e is the original exception object for performing operation on it.
    * */
    fun log(tag: String?, e: Exception) {
        try {
            logger.log(Level.SEVERE, e.message, e)
        } catch (exception: Exception) {
            /* In case the method is throwing an exception*/
            logger.throwing(tag, e.message, e)
        }
    }
}